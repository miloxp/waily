import { useQuery } from "react-query";
import { apiService } from "../services/api";
import { Building2, DollarSign, CreditCard, Activity } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { useNavigate } from "react-router-dom";

export default function PlatformAdminPage() {
  const navigate = useNavigate();
  const { data: businesses, isLoading: businessesLoading } = useQuery(
    "businesses",
    () => apiService.getBusinesses()
  );

  const { data: subscriptions, isLoading: subscriptionsLoading } = useQuery(
    "subscriptions",
    () => apiService.getSubscriptions()
  );

  if (businessesLoading || subscriptionsLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  const activeBusinesses = businesses?.filter((b) => b.isActive).length || 0;
  const activeSubscriptions =
    subscriptions?.filter((s) => s.status === "ACTIVE" || s.status === "TRIAL")
      .length || 0;
  const monthlyRevenue =
    subscriptions?.reduce(
      (sum, s) =>
        s.status === "ACTIVE" || s.status === "TRIAL"
          ? sum + s.monthlyPrice
          : sum,
      0
    ) || 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Panel de Administración de Plataforma
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          Gestión global de todas las empresas y suscripciones
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-3">
        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-2 rounded-lg bg-blue-100">
                <Building2 className="h-6 w-6 text-blue-600" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-500">
                  Negocios Activos
                </p>
                <p className="text-2xl font-semibold text-gray-900">
                  {activeBusinesses}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-2 rounded-lg bg-purple-100">
                <CreditCard className="h-6 w-6 text-purple-600" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-500">
                  Suscripciones Activas
                </p>
                <p className="text-2xl font-semibold text-gray-900">
                  {activeSubscriptions}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-body">
            <div className="flex items-center">
              <div className="p-2 rounded-lg bg-green-100">
                <DollarSign className="h-6 w-6 text-green-600" />
              </div>
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-500">
                  Ingresos Mensuales
                </p>
                <p className="text-2xl font-semibold text-gray-900">
                  ${monthlyRevenue.toFixed(2)}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Quick Actions */}
      <div className="card">
        <div className="card-header">
          <h2 className="text-lg font-medium text-gray-900">
            Acciones Rápidas
          </h2>
        </div>
        <div className="card-body">
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <button
              onClick={() => navigate("/businesses")}
              className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Building2 className="h-5 w-5 text-gray-400 mr-3" />
              <div className="text-left">
                <p className="text-sm font-medium text-gray-900">
                  Crear Negocio
                </p>
                <p className="text-xs text-gray-500">Agregar nuevo cliente</p>
              </div>
            </button>

            <button
              onClick={() => navigate("/businesses")}
              className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Activity className="h-5 w-5 text-gray-400 mr-3" />
              <div className="text-left">
                <p className="text-sm font-medium text-gray-900">
                  Ver Negocios
                </p>
                <p className="text-xs text-gray-500">Gestionar clientes</p>
              </div>
            </button>

            <button
              onClick={() => navigate("/subscriptions")}
              className="flex items-center p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <CreditCard className="h-5 w-5 text-gray-400 mr-3" />
              <div className="text-left">
                <p className="text-sm font-medium text-gray-900">
                  Gestionar Suscripciones
                </p>
                <p className="text-xs text-gray-500">
                  Ver y crear suscripciones
                </p>
              </div>
            </button>
          </div>
        </div>
      </div>

      {/* My Business Clients */}
      <div className="card">
        <div className="card-header">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-medium text-gray-900">Negocios</h2>
            <button
              onClick={() => navigate("/businesses")}
              className="text-sm text-primary-600 hover:text-primary-700"
            >
              Ver todos
            </button>
          </div>
        </div>
        <div className="card-body">
          <div className="space-y-4">
            {businesses?.slice(0, 5).map((business) => (
              <div
                key={business.id}
                className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <div className="flex items-center">
                  <Building2 className="h-5 w-5 text-gray-400 mr-3" />
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {business.name}
                    </p>
                    <p className="text-xs text-gray-500">{business.type}</p>
                    {business.address && (
                      <p className="text-xs text-gray-400">
                        {business.address}
                      </p>
                    )}
                  </div>
                </div>
                <span
                  className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    business.isActive
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {business.isActive ? "Activo" : "Inactivo"}
                </span>
              </div>
            ))}
            {businesses?.length === 0 && (
              <p className="text-sm text-gray-500 text-center py-4">
                No hay clientes de negocios registrados
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
