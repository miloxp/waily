import { useQuery } from "react-query";
import { apiService } from "../services/api";
import { Building2, Plus, Search } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { BusinessType } from "../types";

export default function BusinessesPage() {
  const {
    data: businesses,
    isLoading,
    refetch,
  } = useQuery("businesses", () => apiService.getBusinesses());

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
        <button className="btn-primary">
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
              </div>
            </div>

            <div className="card-footer">
              <div className="flex space-x-2">
                <button className="btn-outline flex-1">Editar</button>
                <button className="btn-danger flex-1">Eliminar</button>
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
    </div>
  );
}
