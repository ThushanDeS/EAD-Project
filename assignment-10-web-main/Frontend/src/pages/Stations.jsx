import { useState, useEffect } from 'react';
import { stationsAPI } from '../api/stations';
import { adminAPI } from '../api/admin';
import { useAuth } from '../context/AuthContext';
import Modal from '../components/common/Modal';
import Pagination from '../components/common/Pagination';
import LoadingSpinner from '../components/common/LoadingSpinner';
import StationsTable from '../components/stations/StationsTable';
import StationForm from '../components/stations/StationForm';

const Stations = () => {
  const { isBackoffice } = useAuth();
  const [stations, setStations] = useState([]);
  const [operators, setOperators] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage] = useState(8);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingStation, setEditingStation] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [stationsData, operatorsData] = await Promise.all([
        stationsAPI.getAll(),
        adminAPI.getActiveOperators()
      ]);
      setStations(stationsData || []);
      setOperators(operatorsData || []);
    } catch (error) {
      console.error('Error fetching data:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleFormSubmit = async (formData) => {
    try {
      const [lat, lng] = formData.Location.split(',').map(coord => parseFloat(coord.trim()));
      const payload = {
        Name: formData.Name,
        Location: { Lat: lat, Lng: lng },
        Type: formData.Type,
        SlotCount: parseInt(formData.SlotCount, 10),
        OperatorIds: formData.OperatorId ? [formData.OperatorId] : [],
        Hours: formData.Hours
      };

      if (editingStation) {
        await stationsAPI.update(editingStation._id, payload);
      } else {
        await stationsAPI.create(payload);
      }
      closeModal();
      fetchData();
    } catch (error) {
      console.error('Error submitting station form:', error);
    }
  };

  const handleToggleStatus = async (station) => {
    const action = station.active ? 'deactivate' : 'reactivate';
    if (!window.confirm(`Are you sure you want to ${action} this station?`)) return;

    try {
      if (action === 'deactivate') {
        await stationsAPI.deactivate(station._id);
      } else {
        await stationsAPI.reactivate(station._id);
      }
      fetchData();
    } catch (error) {
      if (error.response && error.response.status === 409) {
        alert(`Could not ${action} station: ${error.response.data.message}`);
      } else {
        console.error(`Error ${action}ing station:`, error);
        alert(`An unexpected error occurred while ${action}ing the station.`);
      }
    }
  };

  const openCreateModal = () => {
    setEditingStation(null);
    setIsModalOpen(true);
  };

  const openEditModal = (station) => {
    setEditingStation(station);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingStation(null);
  };

  const getAvailableOperators = () => {
    const assignedOperatorIds = stations
      .filter(station => !editingStation || station._id !== editingStation._id)
      .map(station => station.operatorId)
      .filter(Boolean);

    const available = operators.filter(op => !assignedOperatorIds.includes(op._id));

    if (editingStation && editingStation.operatorId) {
      const currentOperator = operators.find(op => op._id === editingStation.operatorId);
      if (currentOperator && !available.some(op => op._id === currentOperator._id)) {
        return [currentOperator, ...available];
      }
    }

    return available;
  };

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentStations = stations.slice(indexOfFirstItem, indexOfLastItem);
  const paginate = (pageNumber) => setCurrentPage(pageNumber);

  if (loading) {
    return <LoadingSpinner />;
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-end items-center">
        {isBackoffice && (
          <button onClick={openCreateModal} className="btn-primary flex items-center">
            Add New Station
          </button>
        )}
      </div>

      <div className="card">
        <StationsTable
          stations={currentStations}
          operators={operators}
          onEdit={openEditModal}
          onToggleStatus={handleToggleStatus}
        />
        <Pagination
          itemsPerPage={itemsPerPage}
          totalItems={stations.length}
          paginate={paginate}
          currentPage={currentPage}
        />
      </div>

      <Modal isOpen={isModalOpen} closeModal={closeModal} title={editingStation ? "Update Station" : "Create New Station"}>
        <StationForm
          onSubmit={handleFormSubmit}
          onCancel={closeModal}
          initialData={editingStation}
          availableOperators={getAvailableOperators()}
        />
      </Modal>
    </div>
  );
};

export default Stations;
