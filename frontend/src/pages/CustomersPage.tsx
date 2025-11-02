import { useState } from "react";
import { useQuery } from "react-query";
import { apiService } from "../services/api";
import { UserCheck, Plus, Search, Phone, Mail } from "lucide-react";
import LoadingSpinner from "../components/LoadingSpinner";
import { Customer } from "../types";
import CustomerForm from "../components/forms/CustomerForm";

export default function CustomersPage() {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | undefined>();

  const {
    data: customers,
    isLoading,
    refetch,
  } = useQuery("customers", () => apiService.getCustomers());

  const handleAdd = () => {
    setSelectedCustomer(undefined);
    setIsFormOpen(true);
  };

  const handleEdit = (customer: Customer) => {
    setSelectedCustomer(customer);
    setIsFormOpen(true);
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
          <h1 className="text-2xl font-bold text-gray-900">Clientes</h1>
          <p className="mt-1 text-sm text-gray-500">
            Gestiona la información de clientes y detalles de contacto
          </p>
        </div>
        <button onClick={handleAdd} className="btn-primary">
          <Plus className="h-4 w-4 mr-2" />
          Agregar Cliente
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
          placeholder="Buscar clientes..."
        />
      </div>

      {/* Customers Grid */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        {customers?.map((customer) => (
          <div key={customer.id} className="card">
            <div className="card-body">
              <div className="flex items-center">
                <div className="p-2 rounded-lg bg-primary-100">
                  <UserCheck className="h-6 w-6 text-primary-600" />
                </div>
                <div className="ml-3">
                  <h3 className="text-lg font-medium text-gray-900">
                    {customer.name || "Cliente Desconocido"}
                  </h3>
                  <p className="text-sm text-gray-500">
                    ID de Cliente: {customer.id.slice(0, 8)}...
                  </p>
                </div>
              </div>

              <div className="mt-4 space-y-2">
                <div className="flex items-center text-sm text-gray-600">
                  <Phone className="h-4 w-4 mr-2" />
                  {customer.phone}
                </div>
                {customer.email && (
                  <div className="flex items-center text-sm text-gray-600">
                    <Mail className="h-4 w-4 mr-2" />
                    {customer.email}
                  </div>
                )}
                <div className="text-sm text-gray-500">
                  Se unió: {new Date(customer.createdAt).toLocaleDateString()}
                </div>
              </div>
            </div>

            <div className="card-footer">
              <div className="flex space-x-2">
                <button
                  onClick={() => handleEdit(customer)}
                  className="btn-outline flex-1"
                >
                  Editar
                </button>
                <button className="btn-outline flex-1">Ver Historial</button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {customers?.length === 0 && (
        <div className="text-center py-12">
          <UserCheck className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-medium text-gray-900">
            Sin clientes
          </h3>
          <p className="mt-1 text-sm text-gray-500">
            Comienza agregando un nuevo cliente.
          </p>
        </div>
      )}

      <CustomerForm
        isOpen={isFormOpen}
        onClose={() => {
          setIsFormOpen(false);
          setSelectedCustomer(undefined);
        }}
        customer={selectedCustomer}
      />
    </div>
  );
}
