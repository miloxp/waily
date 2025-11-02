import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "react-query";
import { apiService } from "../services/api";
import {
  Calendar,
  Plus,
  Search,
  CheckCircle,
  XCircle,
  Clock,
} from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { ReservationStatus, Reservation } from "../types";
import ReservationForm from "../components/forms/ReservationForm";
import toast from "react-hot-toast";

export default function ReservationsPage() {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const queryClient = useQueryClient();

  const {
    data: reservations,
    isLoading,
    refetch,
  } = useQuery("reservations", () => apiService.getReservations());

  const confirmMutation = useMutation(
    (id: string) => apiService.confirmReservation(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("reservations");
        toast.success("Reservación confirmada");
      },
      onError: () => {
        toast.error("Error al confirmar la reservación");
      },
    }
  );

  const cancelMutation = useMutation(
    (id: string) => apiService.cancelReservation(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("reservations");
        toast.success("Reservación cancelada");
      },
      onError: () => {
        toast.error("Error al cancelar la reservación");
      },
    }
  );

  const completeMutation = useMutation(
    (id: string) => apiService.completeReservation(id),
    {
      onSuccess: () => {
        queryClient.invalidateQueries("reservations");
        toast.success("Reservación completada");
      },
      onError: () => {
        toast.error("Error al completar la reservación");
      },
    }
  );

  const handleConfirm = async (id: string) => {
    await confirmMutation.mutateAsync(id);
  };

  const handleCancel = async (id: string) => {
    if (window.confirm("¿Estás seguro de que quieres cancelar esta reservación?")) {
      await cancelMutation.mutateAsync(id);
    }
  };

  const handleComplete = async (id: string) => {
    await completeMutation.mutateAsync(id);
  };

  const getStatusIcon = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.CONFIRMED:
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case ReservationStatus.CANCELLED:
        return <XCircle className="h-4 w-4 text-red-500" />;
      case ReservationStatus.PENDING:
        return <Clock className="h-4 w-4 text-yellow-500" />;
      default:
        return <Calendar className="h-4 w-4 text-gray-500" />;
    }
  };

  const getStatusColor = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.CONFIRMED:
        return "bg-green-100 text-green-800";
      case ReservationStatus.CANCELLED:
        return "bg-red-100 text-red-800";
      case ReservationStatus.PENDING:
        return "bg-yellow-100 text-yellow-800";
      default:
        return "bg-gray-100 text-gray-800";
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
          <h1 className="text-2xl font-bold text-gray-900">Reservaciones</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona las reservaciones de mesas y reservas
          </p>
        </div>
        <button
          onClick={() => setIsFormOpen(true)}
          className="btn-primary"
        >
          <Plus className="h-4 w-4 mr-2" />
          Nueva Reservación
        </button>
      </div>

      {/* Search and Filters */}
      <div className="flex space-x-4">
        <div className="relative flex-1">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Search className="h-5 w-5 text-gray-400" />
          </div>
          <input
            type="text"
            className="input pl-10"
            placeholder="Buscar reservaciones..."
          />
        </div>
        <select className="input w-48">
          <option value="">Todos los Estados</option>
          <option value="PENDING">Pendiente</option>
          <option value="CONFIRMED">Confirmado</option>
          <option value="CANCELLED">Cancelado</option>
          <option value="COMPLETED">Completado</option>
        </select>
      </div>

      {/* Reservations Table */}
      <div className="card">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Negocio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Cliente
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha y Hora
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tamaño del Grupo
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
              {reservations?.map((reservation) => (
                <tr key={reservation.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm font-medium text-gray-900">
                      {reservation.businessName || "Negocio Desconocido"}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {reservation.customerName || "Cliente Desconocido"}
                    </div>
                    <div className="text-sm text-gray-500">
                      {reservation.customerPhone}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">
                      {new Date(
                        reservation.reservationDate
                      ).toLocaleDateString()}
                    </div>
                    <div className="text-sm text-gray-500">
                      {reservation.reservationTime}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {reservation.partySize}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      {getStatusIcon(reservation.status)}
                      <span
                        className={`ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(
                          reservation.status
                        )}`}
                      >
                        {reservation.status}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      {reservation.status === ReservationStatus.PENDING && (
                        <button
                          onClick={() => handleConfirm(reservation.id)}
                          className="text-green-600 hover:text-green-900"
                          disabled={confirmMutation.isLoading}
                        >
                          Confirmar
                        </button>
                      )}
                      {reservation.status === ReservationStatus.CONFIRMED && (
                        <button
                          onClick={() => handleComplete(reservation.id)}
                          className="text-blue-600 hover:text-blue-900"
                          disabled={completeMutation.isLoading}
                        >
                          Completar
                        </button>
                      )}
                      {(reservation.status === ReservationStatus.PENDING ||
                        reservation.status === ReservationStatus.CONFIRMED) && (
                        <button
                          onClick={() => handleCancel(reservation.id)}
                          className="text-red-600 hover:text-red-900"
                          disabled={cancelMutation.isLoading}
                        >
                          Cancelar
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {reservations?.length === 0 && (
        <div className="text-center py-12">
          <Calendar className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin reservaciones
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Comienza creando una nueva reservación.
          </p>
        </div>
      )}

      <ReservationForm
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
      />
    </div>
  );
}
