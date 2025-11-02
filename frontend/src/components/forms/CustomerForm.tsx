import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import { Customer } from "../../types";
import toast from "react-hot-toast";
import Modal from "../Modal";

interface CustomerFormProps {
  isOpen: boolean;
  onClose: () => void;
  customer?: Customer;
}

type CustomerFormData = Omit<Customer, "id" | "createdAt" | "updatedAt">;

export default function CustomerForm({
  isOpen,
  onClose,
  customer,
}: CustomerFormProps) {
  const queryClient = useQueryClient();
  const isEditing = !!customer;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<CustomerFormData>();

  // Reset form when customer prop changes or modal opens (for editing)
  useEffect(() => {
    if (isOpen) {
      if (customer) {
        reset({
          phone: customer.phone,
          name: customer.name || "",
          email: customer.email || "",
        });
      } else {
        reset({
          phone: "",
          name: "",
          email: "",
        });
      }
    }
  }, [customer, isOpen, reset]);

  const createMutation = useMutation(
    (data: CustomerFormData) => apiService.createCustomer(data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("customers");
        toast.success("Cliente creado exitosamente");
        reset();
        onClose();
      },
      onError: () => {
        toast.error("Error al crear el cliente");
      },
    }
  );

  const updateMutation = useMutation(
    (data: CustomerFormData) =>
      apiService.updateCustomer(customer!.id, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("customers");
        toast.success("Cliente actualizado exitosamente");
        onClose();
      },
      onError: () => {
        toast.error("Error al actualizar el cliente");
      },
    }
  );

  const onSubmit = async (data: CustomerFormData) => {
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
      title={isEditing ? "Editar Cliente" : "Nuevo Cliente"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Teléfono *</label>
          <input
            {...register("phone", {
              required: "El teléfono es requerido",
              pattern: {
                value: /^\+?[1-9]\d{1,14}$/,
                message: "Formato de teléfono inválido (ej: +1234567890)",
              },
            })}
            type="tel"
            className={errors.phone ? "input-error" : "input"}
            placeholder="Ej: +1234567890"
          />
          {errors.phone && (
            <p className="mt-1 text-sm text-red-600">{errors.phone.message}</p>
          )}
        </div>

        <div>
          <label className="label">Nombre</label>
          <input
            {...register("name")}
            type="text"
            className="input"
            placeholder="Ej: Juan Pérez"
          />
        </div>

        <div>
          <label className="label">Email</label>
          <input
            {...register("email", {
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: "Email inválido",
              },
            })}
            type="email"
            className={errors.email ? "input-error" : "input"}
            placeholder="Ej: juan@example.com"
          />
          {errors.email && (
            <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
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

