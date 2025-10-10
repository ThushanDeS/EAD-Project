import { useState, useEffect } from 'react';
import { bookingsAPI } from '../api/bookings';
import { adminAPI } from '../api/admin';
import { stationsAPI } from '../api/stations';
import {
  CalendarIcon,
  ClockIcon,
  UserGroupIcon,
  BuildingOffice2Icon,
} from '@heroicons/react/24/outline';
import { formatToSriLankaTime } from '../utils/timezone';
import LoadingSpinner from '../components/common/LoadingSpinner';
import StatCard from '../components/dashboard/StatCard';

const Dashboard = () => {
  const [stats, setStats] = useState({
    pendingBookings: 0,
    futureApprovedBookings: 0,
    activeStations: 0,
    totalStations: 0,
    activeOperators: 0,
    totalOperators: 0,
  });
  const [recentBookings, setRecentBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const [
          pending,
          futureApproved,
          recent,
          allStations,
          allOperators,
          activeOperators,
        ] = await Promise.all([
          bookingsAPI.getPendingCount(),
          bookingsAPI.getFutureApprovedCount(),
          bookingsAPI.getRecent(),
          stationsAPI.getAll(),
          adminAPI.getAllOperators(),
          adminAPI.getActiveOperators(),
        ]);

        const activeStationsCount = allStations.filter(
          (station) => station.active
        ).length;

        setStats({
          pendingBookings: pending.count || 0,
          futureApprovedBookings: futureApproved.count || 0,
          activeStations: activeStationsCount,
          totalStations: allStations.length || 0,
          activeOperators: activeOperators.length || 0,
          totalOperators: allOperators.length || 0,
        });

        setRecentBookings(recent || []);
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError('Failed to load dashboard data. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  const dashboardStats = [
    {
      name: 'Pending Bookings',
      value: stats.pendingBookings,
      icon: ClockIcon,
      color: 'text-orange-600',
      bgColor: 'bg-orange-50',
    },
    {
      name: 'Future Approved Bookings',
      value: stats.futureApprovedBookings,
      icon: CalendarIcon,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
    },
    {
      name: 'Active Stations',
      value: stats.activeStations,
      description: `out of ${stats.totalStations} total`,
      icon: BuildingOffice2Icon,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
    },
    {
      name: 'Active Operators',
      value: stats.activeOperators,
      description: `out of ${stats.totalOperators} total`,
      icon: UserGroupIcon,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
    },
  ];

  if (loading) {
    return <LoadingSpinner />;
  }

  if (error) {
    return (
      <div className="text-center py-24">
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {dashboardStats.map((stat) => (
          <StatCard key={stat.name} stat={stat} />
        ))}
      </div>
      <div className="card">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          Recent Booking Activity
        </h3>
        {recentBookings.length > 0 ? (
          <ul className="space-y-4">
            {recentBookings.map((booking) => (
              <li key={booking._id} className="p-3 border-b border-gray-100 last:border-b-0">
                <div className="flex justify-between items-center">
                  <div>
                    <span className="font-medium text-gray-800">
                      {booking.stationName || 'Unknown Station'}
                    </span>
                    <p className="text-sm text-gray-500 mt-1">
                      NIC: {booking.evNic || 'N/A'}
                    </p>
                  </div>
                  <div className="text-right">
                    <span
                      className={`text-xs px-2.5 py-1 rounded-full font-medium ${booking.status === 'APPROVED'
                        ? 'bg-green-100 text-green-800'
                        : 'bg-gray-100 text-gray-700'
                        }`}
                    >
                      {booking.status}
                    </span>
                    <p className="text-xs text-gray-400 mt-1">
                      {formatToSriLankaTime(booking.startUtc)}
                    </p>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <div className="text-center py-10">
            <p className="text-sm text-gray-600">
              No recent booking activity in the last 3 days.
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Dashboard;
