import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiService } from "../services/api";
import { Building2, Plus, Search, CreditCard } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { Business, BusinessType, Subscription } from "../types";
import BusinessForm from "../components/forms/BusinessForm";
import SubscriptionForm from "../components/forms/SubscriptionForm";
import toast from "react-hot-toast";
import { useAuth } from "../hooks/useAuth";
import { UserRole } from "../types";

export default function BusinessesPage() {
  const { role } = useAuth();
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isSubscriptionFormOpen, setIsSubscriptionFormOpen] = useState(false);
  const [selectedBusiness, setSelectedBusiness] = useState<Business | undefined>();
  const [selectedBusinessForSubscription, setSelectedBusinessForSubscription] = useState<string>("");
  const queryClient = useQueryClient();

  const {
    data: businesses,
    isLoading,
    refetch,
  } = useQuery("businesses", () => apiService.getBusinesses());

  const { data: subscriptions } = useQuery(
    "subscriptions",
    () => apiService.getSubscriptions(),
    { enabled: role === UserRole.PLATFORM_ADMIN }
  );

  // Create a map of businessId -> subscription for quick lookup
  const subscriptionMap = new Map<string, Subscription>();
  subscriptions?.forEach((sub) => {
    subscriptionMap.set(sub.businessId, sub);
  });

  const deleteMutation = useMutation(
    (id: string) => apiService.deleteBusiness(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("businesses");
        toast.success("Negocio eliminado exitosamente");
      },
      onError: () => {
        toast.error("Error al eliminar el negocio");
      },
    }
  );

  const handleEdit = (business: Business) => {
    setSelectedBusiness(business);
    setIsFormOpen(true);
  };

  const handleAdd = () => {
    setSelectedBusiness(undefined);
    setIsFormOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (window.confirm("¿Estás seguro de que quieres eliminar este negocio?")) {
      await deleteMutation.mutateAsync(id);
    }
  };

  const handleCreateSubscription = (businessId: string) => {
    setSelectedBusinessForSubscription(businessId);
    setIsSubscriptionFormOpen(true);
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
          <h1 className="text-2xl font-bold text-gray-900">Negocios</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona los perfiles de tu restaurante y negocio de servicios
          </p>
        </div>
        <button onClick={handleAdd} className="btn-primary">
          <Plus className="h-4 w-4 mr-2" />
          Agregar Negocio
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
          placeholder="Buscar negocios..."
        />
      </div>

      {/* Businesses Grid */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {businesses?.map((business) => (
          <div key={business.id} className="card">
            <div className="card-body">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <div className="p-2 rounded-lg bg-primary-100">
                    <Building2 className="h-6 w-6 text-primary-600" />
                  </div>
                  <div className="ml-3">
                    <h3 className="text-lg font-medium text-gray-900">
                      {business.name}
                    </h3>
                    <p className="text-sm text-gray-500">{business.type}</p>
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

              <div className="mt-4 space-y-2">
                {business.address && (
                  <p className="text-sm text-gray-600">{business.address}</p>
                )}
                {business.phone && (
                  <p className="text-sm text-gray-600">{business.phone}</p>
                )}
                <div className="flex justify-between text-sm text-gray-500">
                  <span>Capacidad: {business.capacity}</span>
                  <span>
                    Servicio Promedio: {business.averageServiceTime}min
                  </span>
                </div>
                {role === UserRole.PLATFORM_ADMIN && (
                  <div className="mt-2 pt-2 border-t border-gray-200">
                    {subscriptionMap.has(business.id) ? (
                      <div className="flex items-center justify-between">
                        <div className="flex items-center">
                          <CreditCard className="h-4 w-4 text-gray-400 mr-2" />
                          <span className="text-xs text-gray-600">
                            Plan: {subscriptionMap.get(business.id)?.plan}
                          </span>
                        </div>
                        <span className={`text-xs px-2 py-1 rounded ${
                          subscriptionMap.get(business.id)?.status === "ACTIVE"
                            ? "bg-green-100 text-green-800"
                            : subscriptionMap.get(business.id)?.status === "TRIAL"
                            ? "bg-blue-100 text-blue-800"
                            : "bg-gray-100 text-gray-800"
                        }`}>
                          {subscriptionMap.get(business.id)?.status}
                        </span>
                      </div>
                    ) : (
                      <button
                        onClick={() => handleCreateSubscription(business.id)}
                        className="flex items-center text-xs text-primary-600 hover:text-primary-700"
                      >
                        <CreditCard className="h-3 w-3 mr-1" />
                        Crear Suscripción
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>

            <div className="card-footer">
              <div className="flex space-x-2">
                <button
                  onClick={() => handleEdit(business)}
                  className="btn-outline flex-1"
                >
                  Editar
                </button>
                <button
                  onClick={() => handleDelete(business.id)}
                  className="btn-danger flex-1"
                  disabled={deleteMutation.isLoading}
                >
                  Eliminar
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {businesses?.length === 0 && (
        <div className="text-center py-12">
          <Building2 className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin negocios
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Comienza creando un nuevo negocio.
          </p>
        </div>
      )}

      <BusinessForm
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setSelectedBusiness(undefined);
        }}
        business={selectedBusiness}
      />

      {role === UserRole.PLATFORM_ADMIN && (
        <SubscriptionForm
          isOpen={isSubscriptionFormOpen}
          onClose={() => {
            setIsSubscriptionFormOpen(false);
            setSelectedBusinessForSubscription("");
          }}
          businessId={selectedBusinessForSubscription || undefined}
        />
      )}
    </div>
  );
}
