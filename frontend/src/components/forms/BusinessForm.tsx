import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import { Business, BusinessType } from "../../types";
import toast from "react-hot-toast";
import Modal from "../Modal";

interface BusinessFormProps {
  isOpen: boolean;
  onClose: () => void;
  business?: Business;
}

type BusinessFormData = Omit<
  Business,
  "id" | "createdAt" | "updatedAt" | "isActive"
> & {
  isActive?: boolean;
};

export default function BusinessForm({
  isOpen,
  onClose,
  business,
}: BusinessFormProps) {
  const queryClient = useQueryClient();
  const isEditing = !!business;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<BusinessFormData>({
    defaultValues: {
      isActive: true,
    },
  });

  // Reset form when business prop changes or modal opens (for editing)
  useEffect(() => {
    if (isOpen) {
      if (business) {
        reset({
          name: business.name,
          type: business.type,
          address: business.address || "",
          phone: business.phone || "",
          email: business.email || "",
          capacity: business.capacity,
          averageServiceTime: business.averageServiceTime,
          isActive: business.isActive,
        });
      } else {
        reset({
          isActive: true,
        });
      }
    }
  }, [business, isOpen, reset]);

  const createMutation = useMutation(
    (data: BusinessFormData) => apiService.createBusiness(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("businesses");
        toast.success("Negocio creado exitosamente");
        reset();
        onClose();
      },
      onError: () => {
        toast.error("Error al crear el negocio");
      },
    }
  );

  const updateMutation = useMutation(
    (data: BusinessFormData) =>
      apiService.updateBusiness(business!.id, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("businesses");
        toast.success("Negocio actualizado exitosamente");
        onClose();
      },
      onError: () => {
        toast.error("Error al actualizar el negocio");
      },
    }
  );

  const onSubmit = async (data: BusinessFormData) => {
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

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Editar Negocio" : "Nuevo Negocio"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Nombre del Negocio *</label>
          <input
            {...register("name", {
              required: "El nombre es requerido",
            })}
            type="text"
            className={errors.name ? "input-error" : "input"}
            placeholder="Ej: Restaurante El Buen Sabor"
          />
          {errors.name && (
            <p className="mt-1 text-sm text-red-600">{errors.name.message}</p>
          )}
        </div>

        <div>
          <label className="label">Tipo de Negocio *</label>
          <select
            {...register("type", {
              required: "El tipo es requerido",
            })}
            className={errors.type ? "input-error" : "input"}
          >
            <option value="">Selecciona un tipo</option>
            {Object.values(BusinessType).map((type) => (
              <option key={type} value={type}>
                {type === BusinessType.RESTAURANT && "Restaurante"}
                {type === BusinessType.BAR && "Bar"}
                {type === BusinessType.CAFE && "Café"}
                {type === BusinessType.SALON && "Salón"}
                {type === BusinessType.SPA && "Spa"}
                {type === BusinessType.OTHER && "Otro"}
              </option>
            ))}
          </select>
          {errors.type && (
            <p className="mt-1 text-sm text-red-600">{errors.type.message}</p>
          )}
        </div>

        <div>
          <label className="label">Dirección</label>
          <input
            {...register("address")}
            type="text"
            className="input"
            placeholder="Ej: 123 Calle Principal, Ciudad"
          />
        </div>

        <div>
          <label className="label">Teléfono</label>
          <input
            {...register("phone")}
            type="tel"
            className="input"
            placeholder="Ej: +1234567890"
          />
        </div>

        <div>
          <label className="label">Email</label>
          <input
            {...register("email")}
            type="email"
            className="input"
            placeholder="Ej: contacto@negocio.com"
          />
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Capacidad *</label>
            <input
              {...register("capacity", {
                required: "La capacidad es requerida",
                min: {
                  value: 1,
                  message: "La capacidad debe ser mayor a 0",
                },
                valueAsNumber: true,
              })}
              type="number"
              min="1"
              className={errors.capacity ? "input-error" : "input"}
              placeholder="50"
            />
            {errors.capacity && (
              <p className="mt-1 text-sm text-red-600">
                {errors.capacity.message}
              </p>
            )}
          </div>

          <div>
            <label className="label">Tiempo de Servicio Promedio (min) *</label>
            <input
              {...register("averageServiceTime", {
                required: "El tiempo promedio es requerido",
                min: {
                  value: 1,
                  message: "El tiempo debe ser mayor a 0",
                },
                valueAsNumber: true,
              })}
              type="number"
              min="1"
              className={errors.averageServiceTime ? "input-error" : "input"}
              placeholder="60"
            />
            {errors.averageServiceTime && (
              <p className="mt-1 text-sm text-red-600">
                {errors.averageServiceTime.message}
              </p>
            )}
          </div>
        </div>

        <div>
          <label className="flex items-center">
            <input
              {...register("isActive")}
              type="checkbox"
              className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            />
            <span className="ml-2 text-sm text-gray-700">Negocio Activo</span>
          </label>
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
              ? "Actualizar"
              : "Crear"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

