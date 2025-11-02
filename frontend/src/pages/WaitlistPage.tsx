import { useState, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiService } from "../services/api";
import { Users, Plus, Search, Bell, UserCheck, Clock } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { WaitlistStatus, UserRole } from "../types";
import AddToWaitlistForm from "../components/forms/AddToWaitlistForm";
import toast from "react-hot-toast";
import { useAuth } from "../hooks/useAuth";

// Extract businessId from JWT token
function getBusinessIdFromToken(): string | null {
  try {
    const token = localStorage.getItem("token");
    if (!token) return null;
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.businessId || null;
  } catch {
    return null;
  }
}

export default function WaitlistPage() {
  const { role } = useAuth();
  const { data: businesses } = useQuery("businesses", () =>
    apiService.getBusinesses()
  );
  const { data: profile } = useQuery("profile", () => apiService.getProfile(), {
    enabled: role !== UserRole.PLATFORM_ADMIN,
  });
  const [selectedBusiness, setSelectedBusiness] = useState<string>("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const queryClient = useQueryClient();

  // Filter businesses based on user role
  const userBusinessId = profile?.business?.id || getBusinessIdFromToken();
  const availableBusinesses =
    role === UserRole.PLATFORM_ADMIN
      ? businesses || []
      : businesses?.filter((b) => b.id === userBusinessId) || [];
  
  // Debug logging (remove in production)
  console.log("WaitlistPage Debug:", {
    role,
    businessesCount: businesses?.length || 0,
    userBusinessId,
    availableBusinessesCount: availableBusinesses.length,
    profileBusiness: profile?.business,
  });

  // Auto-select business for non-platform-admin users
  useEffect(() => {
    if (
      !selectedBusiness &&
      availableBusinesses.length > 0 &&
      role !== UserRole.PLATFORM_ADMIN
    ) {
      const userBusinessId =
        profile?.business?.id || getBusinessIdFromToken();
      if (userBusinessId) {
        setSelectedBusiness(userBusinessId);
      } else if (availableBusinesses.length === 1) {
        setSelectedBusiness(availableBusinesses[0].id);
      }
    }
  }, [availableBusinesses, profile, role, selectedBusiness]);

  const {
    data: waitlist,
    isLoading,
    refetch,
  } = useQuery(
    ["waitlist", selectedBusiness],
    () =>
      selectedBusiness
        ? apiService.getWaitlistByBusiness(selectedBusiness)
        : Promise.resolve([]),
    { enabled: !!selectedBusiness }
  );

  const getStatusIcon = (status: WaitlistStatus) => {
    switch (status) {
      case WaitlistStatus.WAITING:
        return <Clock className="h-4 w-4 text-yellow-500" />;
      case WaitlistStatus.NOTIFIED:
        return <Bell className="h-4 w-4 text-blue-500" />;
      case WaitlistStatus.SEATED:
        return <UserCheck className="h-4 w-4 text-green-500" />;
      default:
        return <Users className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status: WaitlistStatus) => {
    switch (status) {
      case WaitlistStatus.WAITING:
        return "bg-yellow-100 text-yellow-800";
      case WaitlistStatus.NOTIFIED:
        return "bg-blue-100 text-blue-800";
      case WaitlistStatus.SEATED:
        return "bg-green-100 text-green-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const isLoadingBusinesses = !businesses;
  const isLoadingProfile = role !== UserRole.PLATFORM_ADMIN && !profile;

  if (isLoadingBusinesses || isLoadingProfile) {
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
          <h1 className="text-2xl font-bold text-gray-900">Lista de Espera</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona la lista de espera de clientes y el asiento
          </p>
        </div>
        <button
          onClick={() => {
            if (!selectedBusiness) {
              toast.error("Por favor selecciona un negocio primero");
              return;
            }
            setIsFormOpen(true);
          }}
          className="btn-primary"
          disabled={!selectedBusiness}
        >
          <Plus className="h-4 w-4 mr-2" />
          Agregar a Lista de Espera
        </button>
      </div>

      {/* Business Selector */}
      <div className="flex space-x-4">
        <div className="flex-1">
          <label className="label">Seleccionar Negocio</label>
          <select
            value={selectedBusiness}
            onChange={(e) => setSelectedBusiness(e.target.value)}
            className="input"
            disabled={availableBusinesses.length === 0}
          >
            {role === UserRole.PLATFORM_ADMIN && (
              <option value="">Elige un negocio...</option>
            )}
            {availableBusinesses.length === 0 ? (
              <option value="">No hay negocios disponibles</option>
            ) : (
              availableBusinesses.map((business) => (
                <option key={business.id} value={business.id}>
                  {business.name}
                </option>
              ))
            )}
          </select>
        </div>
        <div className="flex-1">
          <label className="label">Buscar</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Search className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              className="input pl-10"
              placeholder="Buscar lista de espera..."
            />
          </div>
        </div>
      </div>

      {selectedBusiness && (
        <>
          {/* Waitlist Stats */}
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
            <div className="card">
              <div className="card-body">
                <div className="flex items-center">
                  <div className="p-2 rounded-lg bg-yellow-100">
                    <Clock className="h-6 w-6 text-yellow-600" />
                  </div>
                  <div className="ml-3">
                    <p className="text-sm font-medium text-gray-500">
                      Esperando
                    </p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {waitlist?.filter(
                        (w) => w.status === WaitlistStatus.WAITING
                      ).length || 0}
                    </p>
                  </div>
                </div>
              </div>
            </div>
            <div className="card">
              <div className="card-body">
                <div className="flex items-center">
                  <div className="p-2 rounded-lg bg-blue-100">
                    <Bell className="h-6 w-6 text-blue-600" />
                  </div>
                  <div className="ml-3">
                    <p className="text-sm font-medium text-gray-500">
                      Notificado
                    </p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {waitlist?.filter(
                        (w) => w.status === WaitlistStatus.NOTIFIED
                      ).length || 0}
                    </p>
                  </div>
                </div>
              </div>
            </div>
            <div className="card">
              <div className="card-body">
                <div className="flex items-center">
                  <div className="p-2 rounded-lg bg-green-100">
                    <UserCheck className="h-6 w-6 text-green-600" />
                  </div>
                  <div className="ml-3">
                    <p className="text-sm font-medium text-gray-500">Sentado</p>
                    <p className="text-2xl font-semibold text-gray-900">
                      {waitlist?.filter(
                        (w) => w.status === WaitlistStatus.SEATED
                      ).length || 0}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Waitlist Table */}
          <div className="card">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Posición
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Cliente
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tamaño del Grupo
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tiempo de Espera
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
                  {waitlist?.map((entry) => (
                    <tr key={entry.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          #{entry.position}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {entry.customerName || "Cliente Desconocido"}
                        </div>
                        <div className="text-sm text-gray-500">
                          {entry.customerPhone}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {entry.partySize}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {entry.estimatedWaitTime
                          ? `${entry.estimatedWaitTime} min`
                          : "N/A"}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          {getStatusIcon(entry.status)}
                          <span
                            className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(
                              entry.status
                            )}`}
                          >
                            {entry.status}
                          </span>
                        </div>
                      </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      {entry.status === WaitlistStatus.WAITING && (
                        <button
                          onClick={async () => {
                            try {
                              await apiService.notifyCustomer(entry.id);
                              queryClient.invalidateQueries(["waitlist", selectedBusiness]);
                              toast.success("Cliente notificado");
                            } catch {
                              toast.error("Error al notificar al cliente");
                            }
                          }}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          Notificar
                        </button>
                      )}
                      {entry.status === WaitlistStatus.NOTIFIED && (
                        <button
                          onClick={async () => {
                            try {
                              await apiService.seatCustomer(entry.id);
                              queryClient.invalidateQueries(["waitlist", selectedBusiness]);
                              toast.success("Cliente sentado");
                            } catch {
                              toast.error("Error al sentar al cliente");
                            }
                          }}
                          className="text-green-600 hover:text-green-900"
                        >
                          Sentar
                        </button>
                      )}
                      <button
                        onClick={async () => {
                          if (window.confirm("¿Cancelar esta entrada de la lista de espera?")) {
                            try {
                              await apiService.cancelWaitlistEntry(entry.id);
                              queryClient.invalidateQueries(["waitlist", selectedBusiness]);
                              toast.success("Entrada cancelada");
                            } catch {
                              toast.error("Error al cancelar");
                            }
                          }
                        }}
                        className="text-red-600 hover:text-red-900"
                      >
                        Cancelar
                      </button>
                    </div>
                  </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}

      {!selectedBusiness && (
        <div className="text-center py-12">
          <Users className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Selecciona un negocio
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Elige un negocio para ver su lista de espera.
          </p>
        </div>
      )}

      {selectedBusiness && waitlist?.length === 0 && (
        <div className="text-center py-12">
          <Users className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin entradas en la lista de espera
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            La lista de espera está vacía para este negocio.
          </p>
        </div>
      )}

      {selectedBusiness && (
        <AddToWaitlistForm
          isOpen={isFormOpen}
          onClose={() => setIsFormOpen(false)}
          businessId={selectedBusiness}
        />
      )}
    </div>
  );
}
