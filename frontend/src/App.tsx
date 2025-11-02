import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "./hooks/useAuth";
import Layout from "./components/Layout";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import PlatformAdminPage from "./pages/PlatformAdminPage";
import BusinessesPage from "./pages/BusinessesPage";
import SubscriptionsPage from "./pages/SubscriptionsPage";
import UsersPage from "./pages/UsersPage";
import ReservationsPage from "./pages/ReservationsPage";
import WaitlistPage from "./pages/WaitlistPage";
import CustomersPage from "./pages/CustomersPage";
import LoadingSpinner from "./components/LoadingSpinner";
import { UserRole } from "./types";

function App() {
  const { isAuthenticated, isLoading, role } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  // Determine default route based on role
  const getDefaultRoute = () => {
    if (role === UserRole.PLATFORM_ADMIN) {
      return "/platform-admin";
    }
    return "/dashboard";
  };

  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to={getDefaultRoute()} replace />} />
        {role === UserRole.PLATFORM_ADMIN && (
          <Route path="/platform-admin" element={<PlatformAdminPage />} />
        )}
        {role !== UserRole.PLATFORM_ADMIN && (
          <Route path="/dashboard" element={<DashboardPage />} />
        )}
        {(role === UserRole.PLATFORM_ADMIN || role === UserRole.BUSINESS_OWNER) && (
          <Route path="/businesses" element={<BusinessesPage />} />
        )}
        {role === UserRole.PLATFORM_ADMIN && (
          <Route path="/subscriptions" element={<SubscriptionsPage />} />
        )}
        {(role === UserRole.PLATFORM_ADMIN || role === UserRole.BUSINESS_OWNER) && (
          <Route path="/users" element={<UsersPage />} />
        )}
        <Route path="/reservations" element={<ReservationsPage />} />
        <Route path="/waitlist" element={<WaitlistPage />} />
        <Route path="/customers" element={<CustomersPage />} />
        <Route path="*" element={<Navigate to={getDefaultRoute()} replace />} />
      </Routes>
    </Layout>
  );
}

export default App;

