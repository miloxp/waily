import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import Modal from "../Modal";
import toast from "react-hot-toast";
import {
  Subscription,
  SubscriptionPlan,
  SubscriptionStatus,
} from "../../types";

interface SubscriptionFormProps {
  isOpen: boolean;
  onClose: () => void;
  subscription?: Subscription;
  businessId?: string;
}

type SubscriptionFormData = Omit<
  Subscription,
  "id" | "createdAt" | "updatedAt" | "businessName"
>;

export default function SubscriptionForm({
  isOpen,
  onClose,
  subscription,
  businessId,
}: SubscriptionFormProps) {
  const queryClient = useQueryClient();
  const isEditing = !!subscription;

  const { data: businesses } = useQuery("businesses", () =>
    apiService.getBusinesses()
  );

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
  } = useForm<SubscriptionFormData>();

  // Reset form when subscription prop changes or modal opens
  useEffect(() => {
    if (isOpen) {
      if (subscription) {
        reset({
          businessId: subscription.businessId,
          plan: subscription.plan,
          status: subscription.status,
          startDate: subscription.startDate.split("T")[0],
          endDate: subscription.endDate
            ? subscription.endDate.split("T")[0]
            : "",
          billingCycleDays: subscription.billingCycleDays,
          monthlyPrice: subscription.monthlyPrice,
          autoRenew: subscription.autoRenew,
          trialEndDate: subscription.trialEndDate
            ? subscription.trialEndDate.split("T")[0]
            : "",
          notes: subscription.notes || "",
        });
      } else {
        const today = new Date().toISOString().split("T")[0];
        const nextMonth = new Date();
        nextMonth.setMonth(nextMonth.getMonth() + 1);
        const nextMonthStr = nextMonth.toISOString().split("T")[0];

        reset({
          businessId: businessId || "",
          plan: SubscriptionPlan.BASIC,
          status: SubscriptionStatus.TRIAL,
          startDate: today,
          endDate: nextMonthStr,
          billingCycleDays: 30,
          monthlyPrice: 0,
          autoRenew: true,
          trialEndDate: nextMonthStr,
          notes: "",
        });
      }
    }
  }, [subscription, isOpen, reset, businessId]);

  const createMutation = useMutation(
    (data: SubscriptionFormData) => apiService.createSubscription(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        queryClient.invalidateQueries("businesses");
        toast.success("Suscripción creada exitosamente");
        reset();
        onClose();
      },
      onError: (error: any) => {
        if (error.response?.status === 409) {
          toast.error("Este negocio ya tiene una suscripción");
        } else {
          toast.error("Error al crear la suscripción");
        }
      },
    }
  );

  const updateMutation = useMutation(
    (data: SubscriptionFormData) =>
      apiService.updateSubscription(subscription!.id, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        queryClient.invalidateQueries("businesses");
        toast.success("Suscripción actualizada exitosamente");
        onClose();
      },
      onError: () => {
        toast.error("Error al actualizar la suscripción");
      },
    }
  );

  const onSubmit = async (data: SubscriptionFormData) => {
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

  const selectedStatus = watch("status");

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Editar Suscripción" : "Nueva Suscripción"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Negocio *</label>
          <select
            {...register("businessId", {
              required: "El negocio es requerido",
            })}
            className={errors.businessId ? "input-error" : "input"}
            disabled={isEditing || !!businessId}
          >
            <option value="">Selecciona un negocio</option>
            {businesses?.map((business) => (
              <option key={business.id} value={business.id}>
                {business.name}
              </option>
            ))}
          </select>
          {errors.businessId && (
            <p className="mt-1 text-sm text-red-600">
              {errors.businessId.message}
            </p>
          )}
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Plan *</label>
            <select
              {...register("plan", {
                required: "El plan es requerido",
              })}
              className={errors.plan ? "input-error" : "input"}
            >
              <option value={SubscriptionPlan.BASIC}>Básico</option>
              <option value={SubscriptionPlan.PRO}>Profesional</option>
              <option value={SubscriptionPlan.ENTERPRISE}>Enterprise</option>
            </select>
            {errors.plan && (
              <p className="mt-1 text-sm text-red-600">{errors.plan.message}</p>
            )}
          </div>

          <div>
            <label className="label">Estado *</label>
            <select
              {...register("status", {
                required: "El estado es requerido",
              })}
              className={errors.status ? "input-error" : "input"}
            >
              <option value={SubscriptionStatus.TRIAL}>Prueba</option>
              <option value={SubscriptionStatus.ACTIVE}>Activo</option>
              <option value={SubscriptionStatus.EXPIRED}>Expirado</option>
              <option value={SubscriptionStatus.SUSPENDED}>Suspendido</option>
              <option value={SubscriptionStatus.CANCELLED}>Cancelado</option>
            </select>
            {errors.status && (
              <p className="mt-1 text-sm text-red-600">
                {errors.status.message}
              </p>
            )}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Fecha de Inicio *</label>
            <input
              {...register("startDate", {
                required: "La fecha de inicio es requerida",
              })}
              type="date"
              className={errors.startDate ? "input-error" : "input"}
            />
            {errors.startDate && (
              <p className="mt-1 text-sm text-red-600">
                {errors.startDate.message}
              </p>
            )}
          </div>

          <div>
            <label className="label">Fecha de Fin</label>
            <input
              {...register("endDate")}
              type="date"
              className="input"
            />
          </div>
        </div>

        {selectedStatus === SubscriptionStatus.TRIAL && (
          <div>
            <label className="label">Fecha de Fin de Prueba</label>
            <input
              {...register("trialEndDate")}
              type="date"
              className="input"
            />
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Ciclo de Facturación (días) *</label>
            <input
              {...register("billingCycleDays", {
                required: "El ciclo de facturación es requerido",
                min: { value: 1, message: "Debe ser mayor a 0" },
                valueAsNumber: true,
              })}
              type="number"
              min="1"
              className={
                errors.billingCycleDays ? "input-error" : "input"
              }
            />
            {errors.billingCycleDays && (
              <p className="mt-1 text-sm text-red-600">
                {errors.billingCycleDays.message}
              </p>
            )}
          </div>

          <div>
            <label className="label">Precio Mensual ($) *</label>
            <input
              {...register("monthlyPrice", {
                required: "El precio mensual es requerido",
                min: { value: 0, message: "Debe ser mayor o igual a 0" },
                valueAsNumber: true,
              })}
              type="number"
              step="0.01"
              min="0"
              className={errors.monthlyPrice ? "input-error" : "input"}
            />
            {errors.monthlyPrice && (
              <p className="mt-1 text-sm text-red-600">
                {errors.monthlyPrice.message}
              </p>
            )}
          </div>
        </div>

        <div>
          <label className="flex items-center">
            <input
              {...register("autoRenew")}
              type="checkbox"
              className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
            />
            <span className="ml-2 text-sm text-gray-700">
              Renovación Automática
            </span>
          </label>
        </div>

        <div>
          <label className="label">Notas</label>
          <textarea
            {...register("notes")}
            rows={3}
            className="input"
            placeholder="Notas adicionales sobre la suscripción..."
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
              ? "Actualizar"
              : "Crear"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

