import { useEffect, useMemo } from "react";
import { useForm } from "react-hook-form";
import { useMutation, useQuery, useQueryClient } from "react-query";
import { apiService } from "../../services/api";
import Modal from "../Modal";
import toast from "react-hot-toast";
import { User, UserRole } from "../../types";
import { useAuth } from "../../hooks/useAuth";

interface UserFormProps {
  isOpen: boolean;
  onClose: () => void;
  user?: User;
}

type UserFormData = Omit<User, "id" | "createdAt" | "updatedAt" | "businessNames"> & {
  password?: string;
};

export default function UserForm({
  isOpen,
  onClose,
  user,
}: UserFormProps) {
  const queryClient = useQueryClient();
  const isEditing = !!user;
  const { role: currentUserRole, businessIds: currentUserBusinessIds } = useAuth();

  const { data: businesses } = useQuery("businesses", () =>
    apiService.getBusinesses()
  );

  // Filter available roles based on current user role
  const availableRoles = useMemo(() => {
    if (currentUserRole === UserRole.PLATFORM_ADMIN) {
      return [UserRole.BUSINESS_OWNER];
    } else if (currentUserRole === UserRole.BUSINESS_OWNER) {
      return [UserRole.BUSINESS_STAFF];
    }
    return [];
  }, [currentUserRole]);

  // Filter businesses available for selection
  const availableBusinesses = useMemo(() => {
    if (currentUserRole === UserRole.PLATFORM_ADMIN) {
      // PLATFORM_ADMIN can assign any business
      return businesses || [];
    } else if (currentUserRole === UserRole.BUSINESS_OWNER) {
      // BUSINESS_OWNER can only assign their own businesses
      return (businesses || []).filter((b) =>
        currentUserBusinessIds.includes(b.id)
      );
    }
    return [];
  }, [businesses, currentUserRole, currentUserBusinessIds]);

  // Check current staff count for BUSINESS_OWNER
  const { data: existingUsers } = useQuery(
    "users",
    () => apiService.getUsers(),
    {
      enabled: currentUserRole === UserRole.BUSINESS_OWNER && !isEditing,
    }
  );

  const currentStaffCount = useMemo(() => {
    if (currentUserRole !== UserRole.BUSINESS_OWNER || !existingUsers) {
      return 0;
    }
    return existingUsers.filter(
      (u) =>
        u.role === UserRole.BUSINESS_STAFF &&
        u.businessIds.some((bid) => currentUserBusinessIds.includes(bid))
    ).length;
  }, [existingUsers, currentUserRole, currentUserBusinessIds]);

  const canCreateMoreStaff = currentStaffCount < 3;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
    setValue,
  } = useForm<UserFormData>();

  // Reset form when user prop changes or modal opens
  useEffect(() => {
    if (isOpen) {
      if (user) {
        reset({
          username: user.username,
          email: user.email,
          role: user.role,
          businessIds: user.businessIds,
          isActive: user.isActive,
          password: "", // Don't pre-fill password
        });
      } else {
        reset({
          username: "",
          email: "",
          role: availableRoles[0] || UserRole.BUSINESS_OWNER,
          businessIds: [],
          isActive: true,
          password: "",
        });
      }
    }
  }, [user, isOpen, reset, availableRoles]);

  const createMutation = useMutation(
    (data: UserFormData) => {
      if (!data.password || data.password.trim() === "") {
        throw new Error("Password is required");
      }
      return apiService.createUser({
        ...data,
        password: data.password!,
      } as any);
    },
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        toast.success("Usuario creado exitosamente");
        reset();
        onClose();
      },
      onError: (error: any) => {
        if (error.response?.status === 409) {
          toast.error("El nombre de usuario o email ya existe");
        } else if (error.response?.status === 403) {
          toast.error("No tienes permiso para crear usuarios con este rol");
        } else if (error.response?.status === 400) {
          toast.error("Se alcanzó el límite máximo de personal (3 usuarios)");
        } else {
          toast.error("Error al crear el usuario");
        }
      },
    }
  );

  const updateMutation = useMutation(
    (data: UserFormData) =>
      apiService.updateUser(user!.id, data),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        toast.success("Usuario actualizado exitosamente");
        onClose();
      },
      onError: (error: any) => {
        if (error.response?.status === 409) {
          toast.error("El nombre de usuario o email ya existe");
        } else {
          toast.error("Error al actualizar el usuario");
        }
      },
    }
  );

  const onSubmit = async (data: UserFormData) => {
    try {
      if (isEditing) {
        await updateMutation.mutateAsync(data);
      } else {
        // Validate staff limit for BUSINESS_OWNER
        if (
          currentUserRole === UserRole.BUSINESS_OWNER &&
          data.role === UserRole.BUSINESS_STAFF &&
          !canCreateMoreStaff
        ) {
          toast.error("Has alcanzado el límite máximo de 3 usuarios de personal");
          return;
        }

        if (!data.password || data.password.trim() === "") {
          toast.error("La contraseña es requerida");
          return;
        }
        await createMutation.mutateAsync(data);
      }
    } catch (error) {
      // Error is handled in mutation callbacks
    }
  };

  const selectedBusinessIds = watch("businessIds") || [];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditing ? "Editar Usuario" : "Nuevo Usuario"}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <label className="label">Nombre de Usuario *</label>
          <input
            {...register("username", {
              required: "El nombre de usuario es requerido",
              minLength: {
                value: 3,
                message: "El nombre de usuario debe tener al menos 3 caracteres",
              },
            })}
            className={errors.username ? "input-error" : "input"}
          />
          {errors.username && (
            <p className="mt-1 text-sm text-red-600">
              {errors.username.message}
            </p>
          )}
        </div>

        <div>
          <label className="label">Email *</label>
          <input
            {...register("email", {
              required: "El email es requerido",
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                message: "Email inválido",
              },
            })}
            type="email"
            className={errors.email ? "input-error" : "input"}
          />
          {errors.email && (
            <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
          )}
        </div>

        {!isEditing && (
          <div>
            <label className="label">Contraseña *</label>
            <input
              {...register("password", {
                required: isEditing ? false : "La contraseña es requerida",
                minLength: {
                  value: 6,
                  message: "La contraseña debe tener al menos 6 caracteres",
                },
              })}
              type="password"
              className={errors.password ? "input-error" : "input"}
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">
                {errors.password.message}
              </p>
            )}
          </div>
        )}

        {isEditing && (
          <div>
            <label className="label">Nueva Contraseña (dejar vacío para mantener la actual)</label>
            <input
              {...register("password", {
                minLength: {
                  value: 6,
                  message: "La contraseña debe tener al menos 6 caracteres",
                },
              })}
              type="password"
              className={errors.password ? "input-error" : "input"}
              placeholder="Solo completa si deseas cambiar la contraseña"
            />
            {errors.password && (
              <p className="mt-1 text-sm text-red-600">
                {errors.password.message}
              </p>
            )}
          </div>
        )}

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="label">Rol *</label>
            <select
              {...register("role", {
                required: "El rol es requerido",
              })}
              className={errors.role ? "input-error" : "input"}
              disabled={isEditing || availableRoles.length === 0}
            >
              {availableRoles.map((role) => (
                <option key={role} value={role}>
                  {role === UserRole.BUSINESS_OWNER
                    ? "Propietario"
                    : role === UserRole.BUSINESS_STAFF
                    ? "Personal"
                    : role}
                </option>
              ))}
            </select>
            {!isEditing &&
              currentUserRole === UserRole.BUSINESS_OWNER &&
              !canCreateMoreStaff && (
                <p className="mt-1 text-sm text-amber-600">
                  Has alcanzado el límite de 3 usuarios de personal. No puedes crear más.
                </p>
              )}
            {errors.role && (
              <p className="mt-1 text-sm text-red-600">{errors.role.message}</p>
            )}
          </div>

          <div>
            <label className="label">Estado</label>
            <select
              {...register("isActive")}
              className="input"
            >
              <option value="true">Activo</option>
              <option value="false">Inactivo</option>
            </select>
          </div>
        </div>

        <div>
          <label className="label">Negocios *</label>
          <input
            type="hidden"
            {...register("businessIds", {
              required: "Debe seleccionar al menos un negocio",
              validate: (value) =>
                value && value.length > 0
                  ? true
                  : "Debe seleccionar al menos un negocio",
            })}
          />
          <div className="max-h-48 overflow-y-auto border border-gray-300 rounded-lg p-2">
            {availableBusinesses.length === 0 ? (
              <p className="text-sm text-gray-500 p-2">
                {currentUserRole === UserRole.BUSINESS_OWNER
                  ? "No tienes negocios asignados"
                  : "No hay negocios disponibles"}
              </p>
            ) : (
              <>
                {availableBusinesses.map((business) => (
                  <label
                    key={business.id}
                    className="flex items-center p-2 hover:bg-gray-50 rounded cursor-pointer"
                  >
                    <input
                      type="checkbox"
                      checked={selectedBusinessIds.includes(business.id)}
                      className="rounded border-gray-300 text-primary-600 focus:ring-primary-500 mr-2 cursor-pointer"
                      onChange={(e) => {
                        const currentValue = selectedBusinessIds;
                        let newValue: string[];
                        if (e.target.checked) {
                          newValue = [...currentValue, business.id];
                        } else {
                          newValue = currentValue.filter((id) => id !== business.id);
                        }
                        setValue("businessIds", newValue, {
                          shouldValidate: true,
                        });
                      }}
                    />
                    <span className="text-sm text-gray-700">{business.name}</span>
                  </label>
                ))}
              </>
            )}
          </div>
          {errors.businessIds && (
            <p className="mt-1 text-sm text-red-600">
              {errors.businessIds.message}
            </p>
          )}
          {selectedBusinessIds.length > 0 && (
            <p className="mt-1 text-sm text-gray-500">
              {selectedBusinessIds.length} negocio(s) seleccionado(s)
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
            disabled={
              isSubmitting ||
              (!isEditing &&
                currentUserRole === UserRole.BUSINESS_OWNER &&
                !canCreateMoreStaff)
            }
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

