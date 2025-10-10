import { useState, useEffect } from "react";
import { bookingsAPI } from "../api/bookings";
import { CalendarIcon, ClockIcon, CheckCircleIcon, XCircleIcon } from "@heroicons/react/24/outline";
import LoadingSpinner from "../components/common/LoadingSpinner";
import StatCard from "../components/operators/StatCard";

const OperatorDashboard = () => {
  const [stats, setStats] = useState({
    pending: 0,
    approved: 0,
    completed: 0,
    cancelled: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const bookings = await bookingsAPI.getMyStationBookings();
        setStats({
          pending: bookings.filter((b) => b.status === "pending").length,
          approved: bookings.filter((b) => b.status === "approved").length,
          completed: bookings.filter((b) => b.status === "completed").length,
          cancelled: bookings.filter((b) => b.status === "cancelled").length,
        });
      } catch (err) {
        console.error(err);
        setError("Failed to load dashboard data. Please try again later.");
      } finally {
        setLoading(false);
      }
    };
    fetchDashboardData();
  }, []);

  const dashboardStats = [
    { name: "Pending Bookings", value: stats.pending, icon: ClockIcon, color: "text-yellow-600", bgColor: "bg-yellow-100" },
    { name: "Approved Bookings", value: stats.approved, icon: CheckCircleIcon, color: "text-blue-600", bgColor: "bg-blue-100" },
    { name: "Completed Bookings", value: stats.completed, icon: CalendarIcon, color: "text-green-700", bgColor: "bg-green-100" },
    { name: "Cancelled Bookings", value: stats.cancelled, icon: XCircleIcon, color: "text-red-600", bgColor: "bg-red-100" },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center h-screen">
        <LoadingSpinner className="border-gray-700" size="h-10 w-10" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-screen text-center">
        <p className="text-red-600 text-lg">{error}</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen w-full bg-gray-50 overflow-hidden">
      <div className="text-center py-10">
        <h1 className="text-4xl font-bold text-gray-900 mb-2">Operator Dashboard</h1>
        <p className="text-gray-600 text-lg">Overview of your bookings and station management</p>
      </div>
      <div className="grid grid-cols-2 grid-rows-2 gap-6 px-10 max-w-6xl mx-auto h-[70vh]">
        {dashboardStats.map((stat) => (
          <StatCard key={stat.name} stat={stat} />
        ))}
      </div>
    </div>
  );
};

export default OperatorDashboard;