import { useQuery } from "react-query";
import { apiService } from "../services/api";
import { useAuth } from "../hooks/useAuth";
import { Building2, ChevronDown } from "lucide-react";
import { UserRole } from "../types";

export default function BusinessSelector() {
  const { role, businessIds, selectedBusinessId, setSelectedBusinessId } = useAuth();

  // Only show selector for business-level roles with multiple businesses
  if (role === UserRole.PLATFORM_ADMIN || businessIds.length <= 1) {
    return null;
  }

  const { data: businesses } = useQuery("businesses", () =>
    apiService.getBusinesses()
  );

  const availableBusinesses = businesses?.filter((b) =>
    businessIds.includes(b.id)
  );

  if (!availableBusinesses || availableBusinesses.length <= 1) {
    return null;
  }

  const selectedBusiness = availableBusinesses.find(
    (b) => b.id === selectedBusinessId
  );

  return (
    <div className="relative">
      <select
        value={selectedBusinessId || ""}
        onChange={(e) => setSelectedBusinessId(e.target.value || null)}
        className="appearance-none bg-white border border-gray-300 rounded-lg px-4 py-2 pr-8 text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-primary-500"
      >
        {availableBusinesses.map((business) => (
          <option key={business.id} value={business.id}>
            {business.name}
          </option>
        ))}
      </select>
      <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
        <ChevronDown className="h-4 w-4" />
      </div>
    </div>
  );
}

