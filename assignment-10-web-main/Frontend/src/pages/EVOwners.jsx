import { useState } from 'react';
import { evOwnersAPI } from '../api/evowners';
import { MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EVOwnersTable from '../components/evowners/EVOwnersTable';
import EVOwnerForm from '../components/evowners/EVOwnerForm';

const EVOwners = () => {
  const [searchNic, setSearchNic] = useState('');
  const [searchedOwners, setSearchedOwners] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingOwner, setEditingOwner] = useState(null);

  const handleSearch = async (e) => {
    if (e) e.preventDefault();
    if (!searchNic) {
      setSearchedOwners([]);
      return;
    }
    setLoading(true);
    try {
      const owners = await evOwnersAPI.search(searchNic);
      setSearchedOwners(owners || []);
    } catch (error) {
      console.error('Error fetching owners:', error);
      setSearchedOwners([]);
    } finally {
      setLoading(false);
    }
  };

  const handleFormSubmit = async (formData) => {
    try {
      if (editingOwner) {
        const updateDto = { Name: formData.Name, Phone: formData.Phone, Email: formData.Email };
        await evOwnersAPI.update(editingOwner.id, updateDto);
        alert('Owner details updated successfully!');
      } else {
        await evOwnersAPI.create(formData);
        alert('EV Owner registered successfully!');
      }
      closeModal();
      if (searchNic) handleSearch();
    } catch (error) {
      console.error('Error submitting form:', error);
      alert(`Error: ${error.response?.data?.message || 'Failed to submit form.'}`);
    }
  };

  const handleToggleStatus = async (owner) => {
    const action = owner.status === 'active' ? 'deactivate' : 'activate';
    try {
      if (action === 'deactivate') {
        await evOwnersAPI.deactivate(owner.id);
      } else {
        await evOwnersAPI.activate(owner.id);
      }
      alert(`EV Owner ${action}d successfully!`);
      if (searchNic) handleSearch();
    } catch (error) {
      console.error(`Error ${action}ing owner:`, error);
      alert(`Failed to ${action} owner.`);
    }
  };

  const openCreateModal = () => {
    setEditingOwner(null);
    setIsModalOpen(true);
  };

  const openEditModal = (owner) => {
    setEditingOwner(owner);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingOwner(null);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-end items-center">
        <button onClick={openCreateModal} className="btn-primary flex items-center">
          Register New EV Owner
        </button>
      </div>

      <div className="card">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Search Owners</h3>
        <form onSubmit={handleSearch} className="flex gap-4">
          <div className="flex-1">
            <input type="text" placeholder="Enter NIC number to search" className="input-field" value={searchNic} onChange={(e) => setSearchNic(e.target.value)} />
          </div>
          <button type="submit" className="btn-primary flex items-center" disabled={loading}>
            <MagnifyingGlassIcon className="h-5 w-5 mr-2" />
            Search
          </button>
        </form>
      </div>

      {searchNic && (
        <div className="card">
          <h3 className="text-lg font-medium text-gray-900 mb-4">Search Results for NIC: {searchNic}</h3>
          {loading ? (
            <LoadingSpinner />
          ) : (
            <EVOwnersTable owners={searchedOwners} onEdit={openEditModal} onToggleStatus={handleToggleStatus} />
          )}
        </div>
      )}

      <Modal isOpen={isModalOpen} closeModal={closeModal} title={editingOwner ? "Edit EV Owner" : "Register New EV Owner"}>
        <EVOwnerForm onSubmit={handleFormSubmit} onCancel={closeModal} initialData={editingOwner} />
      </Modal>
    </div>
  );
};

export default EVOwners;
