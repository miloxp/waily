import { useQuery } from "react-query";
import { apiService } from "../services/api";
import {
  Building2,
  Calendar,
  Users,
  UserCheck,
  TrendingUp,
  Clock,
} from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { useAuth } from "../hooks/useAuth";
import { UserRole } from "../types";

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

export default function DashboardPage() {
  const { role } = useAuth();
  const { data: businesses, isLoading: businessesLoading } = useQuery(
    "businesses",
    () => apiService.getBusinesses(),
    { refetchInterval: 30000 }
  );

  const { data: profile } = useQuery("profile", () => apiService.getProfile(), {
    enabled: role !== UserRole.PLATFORM_ADMIN,
  });

  const { data: reservations, isLoading: reservationsLoading } = useQuery(
    "reservations",
    () => apiService.getReservations(),
    { refetchInterval: 30000 }
  );

  const { data: customers, isLoading: customersLoading } = useQuery(
    "customers",
    () => apiService.getCustomers(),
    { refetchInterval: 30000 }
  );

  // Filter businesses based on user role
  const userBusinessId = profile?.business?.id || getBusinessIdFromToken();
  const availableBusinesses =
    role === UserRole.PLATFORM_ADMIN
      ? businesses || []
      : businesses?.filter((b) => b.id === userBusinessId) || [];

  // Filter reservations and customers for non-platform-admin users
  const availableReservations =
    role === UserRole.PLATFORM_ADMIN
      ? reservations || []
      : reservations?.filter((r) => r.businessId === userBusinessId) || [];

  const availableCustomers =
    role === UserRole.PLATFORM_ADMIN
      ? customers || []
      : customers || []; // Customers might not have businessId, keep all for now

  const isLoading =
    businessesLoading || reservationsLoading || customersLoading;

  const stats = [
    {
      name: role === UserRole.PLATFORM_ADMIN ? "Total de Negocios" : "Mi Negocio",
      value: availableBusinesses.length,
      icon: Building2,
      color: "bg-blue-500",
    },
    {
      name: "Reservaciones Activas",
      value:
        availableReservations?.filter(
          (r) => r.status === "PENDING" || r.status === "CONFIRMED"
        ).length || 0,
      icon: Calendar,
      color: "bg-green-500",
    },
    {
      name: "Total de Clientes",
      value: availableCustomers?.length || 0,
      icon: UserCheck,
      color: "bg-purple-500",
    },
    {
      name: "Reservaciones Pendientes",
      value: availableReservations?.filter((r) => r.status === "PENDING").length || 0,
      icon: Clock,
      color: "bg-yellow-500",
    },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Panel Principal</h1>
        <p className="mt-1 text-sm text-gray-500">
          Resumen de tu sistema de lista de espera y reservaciones
        </p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
          <div key={stat.name} className="card">
            <div className="card-body">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className={`p-3 rounded-md ${stat.color}`}>
                    <stat.icon className="h-6 w-6 text-white" />
                  </div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">
                      {stat.name}
                    </dt>
                    <dd className="text-lg font-medium text-gray-900">
                      {stat.value}
                    </dd>
                  </dl>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Reservations */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">
              Reservaciones Recientes
            </h3>
          </div>
          <div className="card-body">
            {availableReservations && availableReservations.length > 0 ? (
              <div className="space-y-3">
                {availableReservations.slice(0, 5).map((reservation) => (
                  <div
                    key={reservation.id}
                    className="flex items-center justify-between"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {reservation.businessName || "Unknown Business"}
                      </p>
                      <p className="text-sm text-gray-500">
                        {reservation.customerName || reservation.customerPhone}{" "}
                        - {reservation.partySize} personas
                      </p>
                    </div>
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        reservation.status === "CONFIRMED"
                          ? "bg-green-100 text-green-800"
                          : reservation.status === "PENDING"
                          ? "bg-yellow-100 text-yellow-800"
                          : reservation.status === "CANCELLED"
                          ? "bg-red-100 text-red-800"
                          : "bg-gray-100 text-gray-800"
                      }`}
                    >
                      {reservation.status}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-sm text-gray-500">
                No se encontraron reservaciones
              </p>
            )}
          </div>
        </div>

        {/* Business Overview */}
        <div className="card">
          <div className="card-header">
            <h3 className="text-lg font-medium text-gray-900">
              Resumen de Negocios
            </h3>
          </div>
          <div className="card-body">
            {availableBusinesses && availableBusinesses.length > 0 ? (
              <div className="space-y-3">
                {availableBusinesses.slice(0, 5).map((business) => (
                  <div
                    key={business.id}
                    className="flex items-center justify-between"
                  >
                    <div>
                      <p className="text-sm font-medium text-gray-900">
                        {business.name}
                      </p>
                      <p className="text-sm text-gray-500">
                        {business.type} • Capacidad: {business.capacity}
                      </p>
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
              </div>
            ) : (
              <p className="text-sm text-gray-500">
                No se encontraron negocios
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
