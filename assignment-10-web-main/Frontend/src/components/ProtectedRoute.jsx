import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({
  children,
  requireAuth = true,
  allowedRoles = [],
}) => {
  const location = useLocation();
  const { user, loading, isAuthenticated } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600" />
      </div>
    );
  }

  const mustBeAuthed = requireAuth || (allowedRoles && allowedRoles.length > 0);

  if (mustBeAuthed && !isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (
    mustBeAuthed &&
    allowedRoles &&
    allowedRoles.length > 0 &&
    (!user || !user.role || !allowedRoles.includes(user.role))
  ) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;
