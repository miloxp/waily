import { useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import { Reservation, UserRole } from "../../types";
import { useAuth } from "../../hooks/useAuth";
import toast from "react-hot-toast";
import Modal from "../Modal";

interface ReservationFormProps {
  isOpen: boolean;
  onClose: () => void;
  reservation?: Reservation;
}

type ReservationFormData = Omit<
  Reservation,
  "id" | "createdAt" | "updatedAt" | "status" | "businessName" | "customerName" | "customerPhone"
>;

export default function ReservationForm({
  isOpen,
  onClose,
  reservation,
}: ReservationFormProps) {
  const queryClient = useQueryClient();
  const isEditing = !!reservation;
  const { role: currentUserRole, businessIds: currentUserBusinessIds } = useAuth();

  const { data: businesses } = useQuery("businesses", () =>
    apiService.getBusinesses()
  );
  const { data: customers } = useQuery("customers", () =>
    apiService.getCustomers()
  );

  // Filter available businesses based on user role
  const availableBusinesses = useMemo(() => {
    if (currentUserRole === UserRole.PLATFORM_ADMIN) {
      // PLATFORM_ADMIN can create reservations for any business
      return businesses || [];
    } else {
      // Business users can only create reservations for their own businesses
      return (businesses || []).filter((b) =>
        currentUserBusinessIds.includes(b.id)
      );
    }
  }, [businesses, currentUserRole, currentUserBusinessIds]);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
  } = useForm<ReservationFormData>();

  // Reset form when reservation prop changes or modal opens (for editing)
  useEffect(() => {
    if (isOpen) {
      if (reservation) {
        reset({
          businessId: reservation.businessId,
          customerId: reservation.customerId,
          reservationDate: reservation.reservationDate,
          reservationTime: reservation.reservationTime,
          partySize: reservation.partySize,
          specialRequests: reservation.specialRequests || "",
        });
      } else {
        reset({
          businessId: "",
          customerId: "",
          reservationDate: "",
          reservationTime: "",
          partySize: 1,
          specialRequests: "",
        });
      }
    }
  }, [reservation, isOpen, reset]);

  const createMutation = useMutation(
    (data: ReservationFormData) => apiService.createReservation(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("reservations");
        toast.success("Reservación creada exitosamente");
        reset();
        onClose();
      },
      onError: (error: any) => {
        if (error.response?.status === 409) {
          toast.error("Ya existe una reservación para esta fecha y hora");
        } else {
          toast.error("Error al crear la reservación");
        }
      },
    }
  );

  const updateMutation = useMutation(
    (data: ReservationFormData) => {
      // Note: Update reservation endpoint might need to be implemented
      // For now, we'll just show an error
      throw new Error("Reservation update not yet implemented");
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries("reservations");
        toast.success("Reservación actualizada exitosamente");
        reset();
        onClose();
      },
      onError: () => {
        toast.error("Error al actualizar la reservación");
      },
    }
  );

  const onSubmit = async (data: ReservationFormData) => {
    try {
      if (isEditing) {
        await updateMutation.mutateAsync(data);
      } else {
        await createMutation.mutateAsync(data);
      }
    } catch (error) {
      // Error is handled in mutation callbacks
    }
  };

  const selectedBusiness = watch("businessId");

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Editar Reservación" : "Nueva Reservación"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Negocio *</label>
          <select
            {...register("businessId", {
              required: "El negocio es requerido",
            })}
            className={errors.businessId ? "input-error" : "input"}
            disabled={isEditing}
          >
            <option value="">Selecciona un negocio</option>
            {availableBusinesses.length === 0 ? (
              <option value="" disabled>
                No hay negocios disponibles
              </option>
            ) : (
              availableBusinesses.map((business) => (
                <option key={business.id} value={business.id}>
                  {business.name}
                </option>
              ))
            )}
          </select>
          {errors.businessId && (
            <p className="mt-1 text-sm text-red-600">
              {errors.businessId.message}
            </p>
          )}
        </div>

        <div>
          <label className="label">Cliente *</label>
          <select
            {...register("customerId", {
              required: "El cliente es requerido",
            })}
            className={errors.customerId ? "input-error" : "input"}
            disabled={isEditing}
          >
            <option value="">Selecciona un cliente</option>
            {customers?.map((customer) => (
              <option key={customer.id} value={customer.id}>
                {customer.name || "Sin nombre"} - {customer.phone}
              </option>
            ))}
          </select>
          {errors.customerId && (
            <p className="mt-1 text-sm text-red-600">
              {errors.customerId.message}
            </p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Fecha *</label>
            <input
              {...register("reservationDate", {
                required: "La fecha es requerida",
              })}
              type="date"
              className={errors.reservationDate ? "input-error" : "input"}
              min={new Date().toISOString().split("T")[0]}
            />
            {errors.reservationDate && (
              <p className="mt-1 text-sm text-red-600">
                {errors.reservationDate.message}
              </p>
            )}
          </div>

          <div>
            <label className="label">Hora *</label>
            <input
              {...register("reservationTime", {
                required: "La hora es requerida",
              })}
              type="time"
              className={errors.reservationTime ? "input-error" : "input"}
            />
            {errors.reservationTime && (
              <p className="mt-1 text-sm text-red-600">
                {errors.reservationTime.message}
              </p>
            )}
          </div>
        </div>

        <div>
          <label className="label">Tamaño del Grupo *</label>
          <input
            {...register("partySize", {
              required: "El tamaño del grupo es requerido",
              min: {
                value: 1,
                message: "El tamaño debe ser mayor a 0",
              },
              valueAsNumber: true,
            })}
            type="number"
            min="1"
            className={errors.partySize ? "input-error" : "input"}
            placeholder="Ej: 4"
          />
          {errors.partySize && (
            <p className="mt-1 text-sm text-red-600">
              {errors.partySize.message}
            </p>
          )}
        </div>

        <div>
          <label className="label">Solicitudes Especiales</label>
          <textarea
            {...register("specialRequests")}
            className="input"
            rows={3}
            placeholder="Ej: Mesa cerca de la ventana, sin gluten..."
          />
        </div>

        <div className="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            onClick={onClose}
            className="btn-outline"
            disabled={isSubmitting}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting
              ? "Guardando..."
              : isEditing
              ? "Actualizar Reservación"
              : "Crear Reservación"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

