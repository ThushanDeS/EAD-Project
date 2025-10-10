import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout/Layout';

import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Operators from './pages/Operators';
import Stations from './pages/Stations';
import Bookings from './pages/Bookings';
import CreateBooking from './pages/CreateBooking';
import EVOwners from './pages/EVOwners';
import Unauthorized from './pages/Unauthorized';
import OperatorDashboard from './pages/OperatorDashboard';
import MyStation from './pages/MyStation';
import StationBookings from './pages/StationBookings';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* Protected Routes */}
          <Route
            path="/dashboard"
            element={
              <ProtectedRoute allowedRoles={['Backoffice', 'StationOperator']}>
                <Layout>
                  <Dashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          <Route
            path="/operator-dashboard"
            element={
              <ProtectedRoute allowedRoles={['StationOperator']}>
                <Layout>
                  <OperatorDashboard />
                </Layout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/operators"
            element={
              <ProtectedRoute allowedRoles={['Backoffice']}>
                <Layout>
                  <Operators />
                </Layout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/stations"
            element={
              <ProtectedRoute allowedRoles={['Backoffice', 'StationOperator']}>
                <Layout>
                  <Stations />
                </Layout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/my-station"
            element={
              <ProtectedRoute allowedRoles={['StationOperator']}>
                <Layout>
                  <MyStation />
                </Layout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/bookings"
            element={
              <ProtectedRoute allowedRoles={['Backoffice', 'StationOperator']}>
                <Layout>
                  <Bookings />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/create-booking"
            element={
              <ProtectedRoute allowedRoles={['Backoffice']}>
                <Layout>
                  <CreateBooking />
                </Layout>
              </ProtectedRoute>
            }
          />

          <Route
            path="/station-bookings"
            element={
              <ProtectedRoute allowedRoles={['StationOperator']}>
                <Layout>
                  <StationBookings />
                </Layout>
              </ProtectedRoute>
            }
          />
          
          <Route
            path="/ev-owners"
            element={
              <ProtectedRoute allowedRoles={['Backoffice']}>
                <Layout>
                  <EVOwners />
                </Layout>
              </ProtectedRoute>
            }
          />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;