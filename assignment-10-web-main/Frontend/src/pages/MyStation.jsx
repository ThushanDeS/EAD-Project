import { useState, useEffect } from "react";
import { stationsAPI } from "../api/stations";
import { MapPinIcon, BoltIcon, ClockIcon } from "@heroicons/react/24/outline";
import LoadingSpinner from "../components/common/LoadingSpinner";
import InfoRow from "../components/stations/InfoRow";

const MyStation = () => {
  const [station, setStation] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchMyStation = async () => {
      try {
        const data = await stationsAPI.getMyStation();
        setStation(data || null);
      } catch (error) {
        console.error("Error fetching station:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchMyStation();
  }, []);

  if (loading) {
    return <LoadingSpinner />;
  }

  if (!station) {
    return (
      <div className="py-12 text-center text-gray-500">
        No station assigned to you
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-6 py-8">
      <div className="border-b border-gray-200 pb-4 mb-6">
        <h1 className="text-3xl font-semibold text-gray-900">
          {station.name}
        </h1>
        <p className="text-gray-600 mt-1">
          Overview of your assigned charging station
        </p>
      </div>
      <div className="space-y-4">
        <InfoRow icon={BoltIcon} label="Station Type" value={station.type} color={station.type === "DC" ? "text-blue-600" : "text-green-600"} />
        <InfoRow icon={BoltIcon} label="Slots Available" value={station.slotCount} color="text-purple-600" />
        <InfoRow icon={ClockIcon} label="Status" value={station.active ? "Active" : "Inactive"} color={station.active ? "text-green-600" : "text-red-600"} />
        <InfoRow icon={ClockIcon} label="Operating Hours" value={`${station.hours?.open} – ${station.hours?.close}`} color="text-yellow-600" />
        <InfoRow icon={MapPinIcon} label="Location" value={`${station.lat}, ${station.lng}`} color="text-indigo-600" />
      </div>
    </div>
  );
};

export default MyStation;