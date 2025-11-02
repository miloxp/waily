import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiService } from "../services/api";
import { Users, Plus, Search, CheckCircle, XCircle } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { User, UserRole } from "../types";
import UserForm from "../components/forms/UserForm";
import toast from "react-hot-toast";

export default function UsersPage() {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | undefined>();
  const queryClient = useQueryClient();

  const {
    data: users,
    isLoading,
    refetch,
  } = useQuery("users", () => apiService.getUsers(), {
    onSuccess: (data) => {
      console.log("Users loaded:", data);
      data?.forEach((user) => {
        console.log(`User ${user.username}:`, {
          businessIds: user.businessIds,
          businessNames: user.businessNames,
        });
      });
    },
  });

  const getRoleLabel = (role: UserRole): string => {
    switch (role) {
      case UserRole.PLATFORM_ADMIN:
        return "Administrador de Plataforma";
      case UserRole.BUSINESS_OWNER:
        return "Propietario";
      case UserRole.BUSINESS_STAFF:
        return "Personal";
      default:
        return role;
    }
  };

  const getRoleColor = (role: UserRole): string => {
    switch (role) {
      case UserRole.PLATFORM_ADMIN:
        return "bg-purple-100 text-purple-800";
      case UserRole.BUSINESS_OWNER:
        return "bg-blue-100 text-blue-800";
      case UserRole.BUSINESS_STAFF:
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const activateMutation = useMutation(
    (id: string) => apiService.activateUser(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        toast.success("Usuario activado");
      },
      onError: () => {
        toast.error("Error al activar el usuario");
      },
    }
  );

  const deactivateMutation = useMutation(
    (id: string) => apiService.deactivateUser(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        toast.success("Usuario desactivado");
      },
      onError: () => {
        toast.error("Error al desactivar el usuario");
      },
    }
  );

  const deleteMutation = useMutation(
    (id: string) => apiService.deleteUser(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("users");
        toast.success("Usuario eliminado");
      },
      onError: () => {
        toast.error("Error al eliminar el usuario");
      },
    }
  );

  const handleAdd = () => {
    setSelectedUser(undefined);
    setIsFormOpen(true);
  };

  const handleEdit = (user: User) => {
    setSelectedUser(user);
    setIsFormOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (window.confirm("¿Estás seguro de que quieres eliminar este usuario?")) {
      await deleteMutation.mutateAsync(id);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usuarios</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona los usuarios del sistema
          </p>
        </div>
        <button onClick={handleAdd} className="btn-primary">
          <Plus className="h-4 w-4 mr-2" />
          Nuevo Usuario
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="h-5 w-5 text-gray-400" />
        </div>
        <input
          type="text"
          className="input pl-10"
          placeholder="Buscar usuarios..."
        />
      </div>

      {/* Users Table */}
      <div className="card">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Email
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Rol
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Negocios
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {users?.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {user.username}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{user.email}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getRoleColor(
                        user.role
                      )}`}
                    >
                      {getRoleLabel(user.role)}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm">
                      {user.businessNames && user.businessNames.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {user.businessNames.map((name, idx) => (
                            <span
                              key={idx}
                              className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-blue-100 text-blue-800 border border-blue-200"
                            >
                              {name}
                            </span>
                          ))}
                        </div>
                      ) : user.businessIds && user.businessIds.length > 0 ? (
                        <div className="flex flex-wrap gap-1">
                          {user.businessIds.map((id, idx) => (
                            <span
                              key={idx}
                              className="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium bg-gray-100 text-gray-600 border border-gray-200"
                            >
                              {id.substring(0, 8)}...
                            </span>
                          ))}
                        </div>
                      ) : (
                        <span className="text-gray-400 italic">Sin negocios asignados</span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      {user.isActive ? (
                        <CheckCircle className="h-5 w-5 text-green-500" />
                      ) : (
                        <XCircle className="h-5 w-5 text-red-500" />
                      )}
                      <span
                        className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                          user.isActive
                            ? "bg-green-100 text-green-800"
                            : "bg-red-100 text-red-800"
                        }`}
                      >
                        {user.isActive ? "Activo" : "Inactivo"}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleEdit(user)}
                        className="text-blue-600 hover:text-blue-900"
                      >
                        Editar
                      </button>
                      {user.isActive ? (
                        <button
                          onClick={() => deactivateMutation.mutate(user.id)}
                          className="text-yellow-600 hover:text-yellow-900"
                        >
                          Desactivar
                        </button>
                      ) : (
                        <button
                          onClick={() => activateMutation.mutate(user.id)}
                          className="text-green-600 hover:text-green-900"
                        >
                          Activar
                        </button>
                      )}
                      <button
                        onClick={() => handleDelete(user.id)}
                        className="text-red-600 hover:text-red-900"
                      >
                        Eliminar
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {users?.length === 0 && (
        <div className="text-center py-12">
          <Users className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin usuarios
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Comienza creando un nuevo usuario.
          </p>
        </div>
      )}

      <UserForm
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setSelectedUser(undefined);
        }}
        user={selectedUser}
      />
    </div>
  );
}

