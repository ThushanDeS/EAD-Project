import { useState, useEffect } from "react";
import { bookingsAPI } from "../api/bookings";
import { operatorAPI } from "../api/operator";
import LoadingSpinner from "../components/common/LoadingSpinner";
import BookingRow from "../components/stations/BookingRow";

const StationBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [errorMessages, setErrorMessages] = useState({});

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const data = await bookingsAPI.getMyStationBookings();
      setBookings(data || []);
    } catch (error) {
      console.error("Error fetching bookings:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBookings();
  }, []);

  const handleAction = async (id, action, payload) => {
    setErrorMessages((prev) => ({ ...prev, [id]: null }));
    try {
      if (action === "approve") await bookingsAPI.approve(id);
      if (action === "reject") await bookingsAPI.reject(id);
      if (action === "complete") await operatorAPI.finalizeBooking(id, payload);
      await fetchBookings();
    } catch (error) {
      const msg = error.response?.data?.message || "An unexpected error occurred.";
      setErrorMessages((prev) => ({ ...prev, [id]: msg }));
    }
  };

  const handleApproveBooking = async (id) => handleAction(id, "approve");
  const handleRejectBooking = async (id) => handleAction(id, "reject");
  const handleCompleteBooking = async (id) =>
    handleAction(id, "complete", { EnergyKWh: 10, Notes: "Completed by operator" });

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-6xl mx-auto px-6 py-8">
      <h1 className="text-2xl font-semibold text-gray-900 mb-6">
        Station Bookings
      </h1>

      {bookings.length === 0 ? (
        <p className="text-center text-gray-500 py-8">No bookings available</p>
      ) : (
        <div className="overflow-x-auto border border-gray-200 rounded-xl bg-white">
          <table className="min-w-full text-sm text-gray-700">
            <thead className="bg-gray-50 border-b border-gray-200 text-gray-600 text-left">
              <tr>
                <th className="px-6 py-3 font-medium">Slot</th>
                <th className="px-6 py-3 font-medium">Time</th>
                <th className="px-6 py-3 font-medium">Status</th>
                <th className="px-6 py-3 font-medium text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {bookings.map((b) => (
                <BookingRow
                  key={b._id}
                  booking={b}
                  error={errorMessages[b._id]}
                  onApprove={handleApproveBooking}
                  onReject={handleRejectBooking}
                  onComplete={handleCompleteBooking}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default StationBookings;