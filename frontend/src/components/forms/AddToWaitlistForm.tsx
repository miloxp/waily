import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import toast from "react-hot-toast";
import Modal from "../Modal";

interface AddToWaitlistFormProps {
  isOpen: boolean;
  onClose: () => void;
  businessId: string;
}

type AddToWaitlistFormData = {
  customerId: string;
  partySize: number;
};

export default function AddToWaitlistForm({
  isOpen,
  onClose,
  businessId,
}: AddToWaitlistFormProps) {
  const queryClient = useQueryClient();

  const { data: customers } = useQuery("customers", () =>
    apiService.getCustomers()
  );

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<AddToWaitlistFormData>();

  const createMutation = useMutation(
    (data: AddToWaitlistFormData) =>
      apiService.addToWaitlist({
        customerId: data.customerId,
        partySize: data.partySize,
      }),
    {
      onSuccess: () => {
        queryClient.invalidateQueries(["waitlist", businessId]);
        queryClient.invalidateQueries("waitlist");
        toast.success("Cliente agregado a la lista de espera exitosamente");
        reset();
        onClose();
      },
      onError: (error: any) => {
        if (error.response?.status === 409) {
          toast.error("El cliente ya está en la lista de espera");
        } else {
          toast.error("Error al agregar a la lista de espera");
        }
      },
    }
  );

  const onSubmit = async (data: AddToWaitlistFormData) => {
    try {
      await createMutation.mutateAsync(data);
    } catch (error) {
      // Error is handled in mutation callbacks
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Agregar a Lista de Espera"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Cliente *</label>
          <select
            {...register("customerId", {
              required: "El cliente es requerido",
            })}
            className={errors.customerId ? "input-error" : "input"}
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
            {isSubmitting ? "Agregando..." : "Agregar a Lista"}
          </button>
        </div>
      </form>
    </Modal>
  );
}

