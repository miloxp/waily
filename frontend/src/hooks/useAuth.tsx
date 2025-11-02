import {
  useState,
  useEffect,
  createContext,
  useContext,
  ReactNode,
} from "react";
import { apiService } from "../services/api";
import { LoginRequest, LoginResponse, UserRole } from "../types";
import toast from "react-hot-toast";

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: LoginResponse | null;
  role: UserRole | null;
  businessIds: string[];
  selectedBusinessId: string | null;
  setSelectedBusinessId: (businessId: string | null) => void;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
}

function extractRoleFromToken(token: string): UserRole | null {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    // Check if role is in claims
    if (payload.role) {
      return payload.role as UserRole;
    }
    // Fallback: check roles array format
    if (payload.roles) {
      const rolesStr = payload.roles.toString();
      if (rolesStr.includes("PLATFORM_ADMIN")) return UserRole.PLATFORM_ADMIN;
      if (rolesStr.includes("BUSINESS_OWNER")) return UserRole.BUSINESS_OWNER;
      if (rolesStr.includes("BUSINESS_STAFF")) return UserRole.BUSINESS_STAFF;
    }
    return null;
  } catch {
    return null;
  }
}

function extractBusinessIdsFromToken(token: string): string[] {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    if (payload.businessIds && Array.isArray(payload.businessIds)) {
      return payload.businessIds.map((id: any) => String(id));
    }
    // Fallback: check for single businessId (backward compatibility)
    if (payload.businessId) {
      return [String(payload.businessId)];
    }
    return [];
  } catch {
    return [];
  }
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<LoginResponse | null>(null);
  const [role, setRole] = useState<UserRole | null>(null);
  const [businessIds, setBusinessIds] = useState<string[]>([]);
  const [selectedBusinessId, setSelectedBusinessId] = useState<string | null>(
    null
  );

  useEffect(() => {
    const initAuth = async () => {
      const token = localStorage.getItem("token");
      if (token) {
        try {
          const isValid = await apiService.validateToken();
          if (isValid) {
            setIsAuthenticated(true);
            const extractedRole = extractRoleFromToken(token);
            setRole(extractedRole);
            const extractedBusinessIds = extractBusinessIdsFromToken(token);
            setBusinessIds(extractedBusinessIds);
            // Set selected business ID from localStorage or use first business
            const savedBusinessId = localStorage.getItem("selectedBusinessId");
            if (
              savedBusinessId &&
              extractedBusinessIds.includes(savedBusinessId)
            ) {
              setSelectedBusinessId(savedBusinessId);
            } else if (extractedBusinessIds.length > 0) {
              setSelectedBusinessId(extractedBusinessIds[0]);
            }
          } else {
            localStorage.removeItem("token");
            localStorage.removeItem("selectedBusinessId");
            setRole(null);
            setBusinessIds([]);
            setSelectedBusinessId(null);
          }
        } catch (error) {
          localStorage.removeItem("token");
          localStorage.removeItem("selectedBusinessId");
          setRole(null);
          setBusinessIds([]);
          setSelectedBusinessId(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  // Save selected business ID to localStorage when it changes
  useEffect(() => {
    if (selectedBusinessId) {
      localStorage.setItem("selectedBusinessId", selectedBusinessId);
    } else {
      localStorage.removeItem("selectedBusinessId");
    }
  }, [selectedBusinessId]);

  const login = async (credentials: LoginRequest) => {
    try {
      const response = await apiService.login(credentials);
      localStorage.setItem("token", response.token);
      setUser(response);
      const extractedRole = extractRoleFromToken(response.token);
      setRole(extractedRole);
      const extractedBusinessIds = extractBusinessIdsFromToken(response.token);
      setBusinessIds(extractedBusinessIds);
      // Set selected business ID from localStorage or use first business
      const savedBusinessId = localStorage.getItem("selectedBusinessId");
      if (savedBusinessId && extractedBusinessIds.includes(savedBusinessId)) {
        setSelectedBusinessId(savedBusinessId);
      } else if (extractedBusinessIds.length > 0) {
        setSelectedBusinessId(extractedBusinessIds[0]);
      } else {
        setSelectedBusinessId(null);
      }
      setIsAuthenticated(true);
      toast.success("¡Inicio de sesión exitoso!");
    } catch (error: any) {
      toast.error(
        error.response?.data?.message || "Error en el inicio de sesión"
      );
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("selectedBusinessId");
    setUser(null);
    setRole(null);
    setBusinessIds([]);
    setSelectedBusinessId(null);
    setIsAuthenticated(false);
    toast.success("Sesión cerrada exitosamente");
  };

  const handleSetSelectedBusinessId = (businessId: string | null) => {
    if (businessId && businessIds.includes(businessId)) {
      setSelectedBusinessId(businessId);
    } else if (businessId === null) {
      setSelectedBusinessId(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        user,
        role,
        businessIds,
        selectedBusinessId,
        setSelectedBusinessId: handleSetSelectedBusinessId,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth debe ser usado dentro de un AuthProvider");
  }
  return context;
}
