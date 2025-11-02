import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiService } from "../services/api";
import {
  CreditCard,
  Plus,
  Search,
  CheckCircle,
  XCircle,
  Clock,
  Ban,
} from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { Subscription, SubscriptionStatus } from "../types";
import SubscriptionForm from "../components/forms/SubscriptionForm";
import toast from "react-hot-toast";

export default function SubscriptionsPage() {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedSubscription, setSelectedSubscription] = useState<
    Subscription | undefined
  >();
  const [selectedBusinessId, setSelectedBusinessId] = useState<string>("");
  const queryClient = useQueryClient();

  const {
    data: subscriptions,
    isLoading,
    refetch,
  } = useQuery("subscriptions", () => apiService.getSubscriptions());

  const getStatusIcon = (status: SubscriptionStatus) => {
    switch (status) {
      case SubscriptionStatus.ACTIVE:
        return <CheckCircle className="h-5 w-5 text-green-500" />;
      case SubscriptionStatus.TRIAL:
        return <Clock className="h-5 w-5 text-blue-500" />;
      case SubscriptionStatus.EXPIRED:
        return <XCircle className="h-5 w-5 text-red-500" />;
      case SubscriptionStatus.SUSPENDED:
        return <Ban className="h-5 w-5 text-yellow-500" />;
      case SubscriptionStatus.CANCELLED:
        return <XCircle className="h-5 w-5 text-gray-500" />;
      default:
        return <Clock className="h-5 w-5 text-gray-500" />;
    }
  };

  const getStatusColor = (status: SubscriptionStatus) => {
    switch (status) {
      case SubscriptionStatus.ACTIVE:
        return "bg-green-100 text-green-800";
      case SubscriptionStatus.TRIAL:
        return "bg-blue-100 text-blue-800";
      case SubscriptionStatus.EXPIRED:
        return "bg-red-100 text-red-800";
      case SubscriptionStatus.SUSPENDED:
        return "bg-yellow-100 text-yellow-800";
      case SubscriptionStatus.CANCELLED:
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getPlanColor = (plan: string) => {
    switch (plan) {
      case "ENTERPRISE":
        return "bg-purple-100 text-purple-800";
      case "PRO":
        return "bg-blue-100 text-blue-800";
      case "BASIC":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const activateMutation = useMutation(
    (id: string) => apiService.activateSubscription(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        toast.success("Suscripción activada");
      },
      onError: () => {
        toast.error("Error al activar la suscripción");
      },
    }
  );

  const cancelMutation = useMutation(
    (id: string) => apiService.cancelSubscription(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        toast.success("Suscripción cancelada");
      },
      onError: () => {
        toast.error("Error al cancelar la suscripción");
      },
    }
  );

  const suspendMutation = useMutation(
    (id: string) => apiService.suspendSubscription(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        toast.success("Suscripción suspendida");
      },
      onError: () => {
        toast.error("Error al suspender la suscripción");
      },
    }
  );

  const deleteMutation = useMutation(
    (id: string) => apiService.deleteSubscription(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("subscriptions");
        toast.success("Suscripción eliminada");
      },
      onError: () => {
        toast.error("Error al eliminar la suscripción");
      },
    }
  );

  const handleAdd = () => {
    setSelectedSubscription(undefined);
    setSelectedBusinessId("");
    setIsFormOpen(true);
  };

  const handleAddForBusiness = (businessId: string) => {
    setSelectedSubscription(undefined);
    setSelectedBusinessId(businessId);
    setIsFormOpen(true);
  };

  const handleEdit = (subscription: Subscription) => {
    setSelectedSubscription(subscription);
    setSelectedBusinessId("");
    setIsFormOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (
      window.confirm(
        "¿Estás seguro de que quieres eliminar esta suscripción?"
      )
    ) {
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
          <h1 className="text-2xl font-bold text-gray-900">Suscripciones</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona las suscripciones de tus clientes de negocios
          </p>
        </div>
        <button onClick={handleAdd} className="btn-primary">
          <Plus className="h-4 w-4 mr-2" />
          Nueva Suscripción
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
          placeholder="Buscar suscripciones..."
        />
      </div>

      {/* Subscriptions Table */}
      <div className="card">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Negocio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Plan
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Inicio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Fin
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Precio Mensual
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Renovación
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {subscriptions?.map((subscription) => (
                <tr key={subscription.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {subscription.businessName || "N/A"}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getPlanColor(
                        subscription.plan
                      )}`}
                    >
                      {subscription.plan}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      {getStatusIcon(subscription.status)}
                      <span
                        className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(
                          subscription.status
                        )}`}
                      >
                        {subscription.status}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {new Date(subscription.startDate).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {subscription.endDate
                      ? new Date(subscription.endDate).toLocaleDateString()
                      : "N/A"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    ${subscription.monthlyPrice.toFixed(2)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {subscription.autoRenew ? "Sí" : "No"}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleEdit(subscription)}
                        className="text-blue-600 hover:text-blue-900"
                      >
                        Editar
                      </button>
                      {subscription.status !== SubscriptionStatus.ACTIVE && (
                        <button
                          onClick={() =>
                            activateMutation.mutate(subscription.id)
                          }
                          className="text-green-600 hover:text-green-900"
                        >
                          Activar
                        </button>
                      )}
                      {subscription.status === SubscriptionStatus.ACTIVE && (
                        <button
                          onClick={() =>
                            suspendMutation.mutate(subscription.id)
                          }
                          className="text-yellow-600 hover:text-yellow-900"
                        >
                          Suspender
                        </button>
                      )}
                      {subscription.status !== SubscriptionStatus.CANCELLED && (
                        <button
                          onClick={() =>
                            cancelMutation.mutate(subscription.id)
                          }
                          className="text-orange-600 hover:text-orange-900"
                        >
                          Cancelar
                        </button>
                      )}
                      <button
                        onClick={() => handleDelete(subscription.id)}
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

      {subscriptions?.length === 0 && (
        <div className="text-center py-12">
          <CreditCard className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin suscripciones
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Comienza creando una nueva suscripción.
          </p>
        </div>
      )}

      <SubscriptionForm
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setSelectedSubscription(undefined);
          setSelectedBusinessId("");
        }}
        subscription={selectedSubscription}
        businessId={selectedBusinessId || undefined}
      />
    </div>
  );
}

