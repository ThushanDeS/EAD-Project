import { useState, useEffect } from 'react';
import { bookingsAPI } from '../api/bookings';
import { stationsAPI } from '../api/stations';
import { evOwnersAPI } from '../api/evowners';
import { useAuth } from '../context/AuthContext';
import { ClockIcon, BuildingOfficeIcon, UserIcon } from '@heroicons/react/24/outline';
import { getCurrentSriLankaTime } from '../utils/timezone';

const CreateBooking = () => {
  const { isBackoffice } = useAuth();
  const [loading, setLoading] = useState(false);
  const [stations, setStations] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [selectedOwner, setSelectedOwner] = useState(null);
  const [selectedStation, setSelectedStation] = useState(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [formData, setFormData] = useState({
    ownerId: '',
    stationId: '',
    startTime: '',
    endTime: '',
    slotNumber: '',
    ownerNic: ''
  });
  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState('');

  useEffect(() => {
    fetchStations();
  }, []);

  const fetchStations = async () => {
    try {
      const data = await stationsAPI.getAll();
      setStations(data || []);
    } catch (error) {
      console.error('Error fetching stations:', error);
    }
  };

  const searchOwners = async (nic) => {
    if (!nic || nic.length < 3) {
      setSearchResults([]);
      return;
    }

    setSearchLoading(true);
    try {
      const data = await evOwnersAPI.search(nic);
      setSearchResults(data || []);
    } catch (error) {
      console.error('Error searching owners:', error);
      setSearchResults([]);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleOwnerSelect = (owner) => {
    setSelectedOwner(owner);
    setFormData(prev => ({
      ...prev,
      ownerId: owner.id || owner._id,
      ownerNic: owner.nic
    }));
    setSearchResults([]);
    setErrors(prev => ({ ...prev, ownerId: '' }));

  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    if (name === 'ownerNic') {
      if (selectedOwner && value !== selectedOwner.nic) {
        setSelectedOwner(null);
        setFormData(prev => ({ ...prev, ownerId: '' }));
      }
      searchOwners(value);
    }

    if (name === 'stationId') {
      const station = stations.find(s => s._id === value);
      setSelectedStation(station);
      setFormData(prev => ({ ...prev, slotNumber: '' }));
    }

    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const getSlotOptions = () => {
    if (!selectedStation) return [];
    const slotCount = selectedStation.slotCount || 1;
    const slots = [];
    for (let i = 1; i <= slotCount; i++) {
      slots.push({ value: i, label: `Slot ${i}` });
    }
    return slots;
  };

  const validateForm = () => {
    const newErrors = {};
    if (!formData.ownerId) newErrors.ownerId = 'Please select an EV owner';
    if (!formData.stationId) newErrors.stationId = 'Please select a station';
    if (!formData.startTime) newErrors.startTime = 'Start time is required';
    if (!formData.endTime) newErrors.endTime = 'End time is required';
    if (formData.startTime && formData.endTime) {
      const start = new Date(formData.startTime);
      const end = new Date(formData.endTime);
      if (start >= end) newErrors.endTime = 'End time must be after start time';
      if (start < new Date()) newErrors.startTime = 'Start time cannot be in the past';
    }
    if (!formData.slotNumber || formData.slotNumber < 1) newErrors.slotNumber = 'Please select a slot';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) return;
    setLoading(true);
    setSuccess('');
    setErrors({});
    try {
      const convertToUTCString = (localDateTimeString) => {
        const localDate = new Date(localDateTimeString);
        const sriLankaOffsetMs = 5.5 * 60 * 60 * 1000;
        const adjustedDate = new Date(localDate.getTime() + sriLankaOffsetMs);
        return adjustedDate.toISOString();
      };

      const bookingData = {
        ownerId: formData.ownerId,
        stationId: formData.stationId,
        startTime: convertToUTCString(formData.startTime),
        endTime: convertToUTCString(formData.endTime),
        slotNumber: parseInt(formData.slotNumber)
      };

      await bookingsAPI.backofficeCreate(bookingData);
      setSuccess('Booking created successfully!');
      setFormData({ ownerId: '', stationId: '', startTime: '', endTime: '', slotNumber: '', ownerNic: '' });
      setSelectedOwner(null);
      setSelectedStation(null);
      setSearchResults([]);
    } catch (error) {
      console.error('Error creating booking:', error);
      setErrors({ submit: error.response?.data?.message || 'Failed to create booking. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  const getMinDateTime = () => {
    return getCurrentSriLankaTime();
  };

  if (!isBackoffice) {
    return (
      <div className="flex items-center justify-center py-12">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900">Access Denied</h2>
          <p className="mt-2 text-gray-600">You don't have permission to access this page.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-4">
        <div>
          <p className="text-gray-600">Create a new booking for an EV owner</p>
        </div>
      </div>

      {success && (
        <div className="p-4 bg-green-100 border border-green-300 rounded-lg">
          <p className="text-green-800">{success}</p>
        </div>
      )}

      {errors.submit && (
        <div className="p-4 bg-red-100 border border-red-300 rounded-lg">
          <p className="text-red-800">{errors.submit}</p>
        </div>
      )}

      <div className="bg-white rounded-lg shadow-sm border border-gray-200">
        <div className="p-6">
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label htmlFor="ownerNic" className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <UserIcon className="h-4 w-4 mr-2" />
                EV Owner (Search by NIC)
              </label>
              <div className="relative">
                <input
                  type="text"
                  id="ownerNic"
                  name="ownerNic"
                  value={formData.ownerNic}
                  onChange={handleInputChange}
                  placeholder="Enter NIC to search for EV owner..."
                  className={`input-field ${errors.ownerId ? 'border-red-300' : 'border-gray-300'}`}
                />

                {searchResults.length > 0 && (
                  <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg">
                    {searchResults.map((owner) => (
                      <button
                        key={owner._id}
                        type="button"
                        onClick={() => handleOwnerSelect(owner)}
                        className="w-full px-4 py-2 text-left hover:bg-gray-50 focus:bg-gray-50 focus:outline-none first:rounded-t-lg last:rounded-b-lg"
                      >
                        <div className="font-medium text-gray-900">{owner.name}</div>
                        <div className="text-sm text-gray-600">NIC: {owner.nic}</div>
                      </button>
                    ))}
                  </div>
                )}

                {searchLoading && (
                  <div className="absolute right-3 top-3">
                    <div className="w-4 h-4 border-b-2 rounded-full animate-spin border-primary-600"></div>
                  </div>
                )}
              </div>

              {selectedOwner && (
                <div className="mt-2 p-3 bg-green-50 border border-green-200 rounded-lg">
                  <div className="flex items-center">
                    <UserIcon className="h-5 w-5 text-green-600 mr-2" />
                    <div>
                      <p className="font-medium text-green-800">{selectedOwner.name}</p>
                      <p className="text-sm text-green-600">NIC: {selectedOwner.nic}</p>
                    </div>
                  </div>
                </div>
              )}

              {errors.ownerId && (
                <p className="mt-1 text-sm text-red-600">{errors.ownerId}</p>
              )}
            </div>

            <div>
              <label htmlFor="stationId" className="flex items-center text-sm font-medium text-gray-700 mb-2">
                <BuildingOfficeIcon className="h-4 w-4 mr-2" />
                Charging Station
              </label>
              <select
                id="stationId"
                name="stationId"
                value={formData.stationId}
                onChange={handleInputChange}
                className={`input-field ${errors.stationId ? 'border-red-300' : 'border-gray-300'}`}
              >
                <option value="">Select a station...</option>
                {stations.map((station) => (
                  <option key={station._id} value={station._id}>
                    {station.name} - {station.address || `${station.lat}, ${station.lng}`} - {station.type || 'AC'}
                  </option>
                ))}
              </select>
              {errors.stationId && (
                <p className="mt-1 text-sm text-red-600">{errors.stationId}</p>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="startTime" className="flex items-center text-sm font-medium text-gray-700 mb-2">
                  <ClockIcon className="h-4 w-4 mr-2" />
                  Start Time
                </label>
                <input
                  type="datetime-local"
                  id="startTime"
                  name="startTime"
                  value={formData.startTime}
                  onChange={handleInputChange}
                  min={getMinDateTime()}
                  className={`input-field ${errors.startTime ? 'border-red-300' : 'border-gray-300'}`}
                />
                {errors.startTime && (
                  <p className="mt-1 text-sm text-red-600">{errors.startTime}</p>
                )}
              </div>

              <div>
                <label htmlFor="endTime" className="flex items-center text-sm font-medium text-gray-700 mb-2">
                  <ClockIcon className="h-4 w-4 mr-2" />
                  End Time
                </label>
                <input
                  type="datetime-local"
                  id="endTime"
                  name="endTime"
                  value={formData.endTime}
                  onChange={handleInputChange}
                  min={formData.startTime || getMinDateTime()}
                  className={`input-field ${errors.endTime ? 'border-red-300' : 'border-gray-300'}`}
                />
                {errors.endTime && (
                  <p className="mt-1 text-sm text-red-600">{errors.endTime}</p>
                )}
              </div>
            </div>

            <div>
              <label htmlFor="slotNumber" className="block text-sm font-medium text-gray-700 mb-2">
                Slot Number
                {selectedStation && (
                  <span className="text-sm font-normal text-gray-500 ml-2">
                    (Available: 1-{selectedStation.slotCount})
                  </span>
                )}
              </label>
              <select
                id="slotNumber"
                name="slotNumber"
                value={formData.slotNumber}
                onChange={handleInputChange}
                disabled={!selectedStation}
                className={`input-field ${errors.slotNumber ? 'border-red-300' : 'border-gray-300'} ${!selectedStation ? 'bg-gray-100 cursor-not-allowed' : ''}`}
              >
                <option value="">
                  {selectedStation ? 'Select a slot...' : 'Select a station first'}
                </option>
                {getSlotOptions().map((slot) => (
                  <option key={slot.value} value={slot.value}>
                    {slot.label}
                  </option>
                ))}
              </select>
              {errors.slotNumber && (
                <p className="mt-1 text-sm text-red-600">{errors.slotNumber}</p>
              )}
            </div>

            <div className="flex items-center justify-end space-x-4 pt-6 border-t border-gray-200">
              <button
                type="submit"
                disabled={loading}
                className="btn-primary flex items-center"
              >
                {loading ? (
                  <>
                    <div className="w-4 h-4 border-b-2 rounded-full animate-spin border-white mr-2"></div>
                    Creating...
                  </>
                ) : (
                  'Create Booking'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateBooking;
