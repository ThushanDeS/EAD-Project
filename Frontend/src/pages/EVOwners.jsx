import { useState } from "react";
import { evOwnersAPI } from "../api/evowners";
import { PlusIcon, MagnifyingGlassIcon } from "@heroicons/react/24/outline";
import Modal from "../components/common/Modal";

const EVOwners = () => {
  const [searchNic, setSearchNic] = useState("");
  const [searchedOwners, setSearchedOwners] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isRegisterModalOpen, setIsRegisterModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [editingOwner, setEditingOwner] = useState(null);
  const [newOwner, setNewOwner] = useState({
    Nic: "",
    Name: "",
    Phone: "",
    Email: "",
    Password: "",
    ConfirmPassword: "",
  });

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!searchNic) {
      setSearchedOwners([]);
      return;
    }

    setLoading(true);
    try {
      const owners = await evOwnersAPI.search(searchNic);
      setSearchedOwners(owners || []);
    } catch (error) {
      console.error("Error fetching owners:", error);
      setSearchedOwners([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateOwner = async (e) => {
    e.preventDefault();
    // Client-side validation: password match and minimum length
    if ((newOwner.Password || "").length < 6) {
      alert("Password must be at least 6 characters long.");
      return;
    }

    if (newOwner.Password !== newOwner.ConfirmPassword) {
      alert("Password and Confirm Password do not match.");
      return;
    }

    try {
      // Only send fields the API expects; ConfirmPassword is for client-side validation only
      const payload = {
        Nic: newOwner.Nic,
        Name: newOwner.Name,
        Phone: newOwner.Phone,
        Email: newOwner.Email,
        Password: newOwner.Password,
      };

      await evOwnersAPI.create(payload);
      setIsRegisterModalOpen(false);
      setNewOwner({
        Nic: "",
        Name: "",
        Phone: "",
        Email: "",
        Password: "",
        ConfirmPassword: "",
      });
      alert("EV Owner registered successfully!");
      if (searchNic) {
        handleSearch({ preventDefault: () => {} });
      }
    } catch (error) {
      console.error("Error creating EV owner:", error);
      alert(
        `Error: ${error.response?.data?.message || "Failed to register owner."}`
      );
    }
  };

  const handleUpdateOwner = async (e) => {
    e.preventDefault();
    if (!editingOwner) return;

    try {
      const updateDto = {
        Name: editingOwner.name,
        Phone: editingOwner.phone,
        Email: editingOwner.email,
      };
      await evOwnersAPI.update(editingOwner.id, updateDto);
      alert("Owner details updated successfully!");
      setIsEditModalOpen(false);
      setEditingOwner(null);
      handleSearch({ preventDefault: () => {} });
    } catch (error) {
      console.error("Error updating owner:", error);
      alert(
        `Error: ${error.response?.data?.message || "Failed to update owner."}`
      );
    }
  };

  const handleInputChange = (e) => {
    setNewOwner({
      ...newOwner,
      [e.target.name]: e.target.value,
    });
  };

  const handleEditFormChange = (e) => {
    setEditingOwner({
      ...editingOwner,
      [e.target.name]: e.target.value,
    });
  };

  const handleActivateOwner = async (ownerId) => {
    try {
      await evOwnersAPI.activate(ownerId);
      alert("EV Owner activated successfully!");
      handleSearch({ preventDefault: () => {} });
    } catch (error) {
      console.error("Error activating owner:", error);
      alert("Failed to activate owner.");
    }
  };

  const handleDeactivateOwner = async (ownerId) => {
    try {
      await evOwnersAPI.deactivate(ownerId);
      alert("EV Owner deactivated successfully!");
      handleSearch({ preventDefault: () => {} });
    } catch (error) {
      console.error("Error deactivating owner:", error);
      alert("Failed to deactivate owner.");
    }
  };

  const openEditModal = (owner) => {
    setEditingOwner(owner);
    setIsEditModalOpen(true);
  };

  const getStatusColor = (status) => {
    if (status?.startsWith("deactivated")) {
      return "bg-red-100 text-red-800";
    }
    switch (status) {
      case "active":
        return "bg-green-100 text-green-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-end">
        <button
          onClick={() => setIsRegisterModalOpen(true)}
          className="flex items-center btn-primary"
        >
          <PlusIcon className="w-5 h-5 mr-2" />
          Register EV Owner
        </button>
      </div>

      <div className="card">
        <h3 className="mb-4 text-lg font-medium text-gray-900">
          Search Owners
        </h3>
        <form onSubmit={handleSearch} className="flex gap-4">
          <div className="flex-1">
            <input
              type="text"
              placeholder="Enter NIC number to search"
              className="input-field"
              value={searchNic}
              onChange={(e) => setSearchNic(e.target.value)}
            />
          </div>
          <button
            type="submit"
            className="flex items-center btn-primary"
            disabled={loading}
          >
            <MagnifyingGlassIcon className="w-5 h-5 mr-2" />
            Search
          </button>
        </form>
      </div>

      {searchNic && (
        <div className="card">
          <h3 className="mb-4 text-lg font-medium text-gray-900">
            Search Results for NIC: {searchNic}
          </h3>

          {loading ? (
            <div className="py-8 text-center">
              <div className="w-8 h-8 mx-auto border-b-2 rounded-full animate-spin border-primary-600"></div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      NIC
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Name
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Email
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Phone
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Status
                    </th>
                    <th className="px-6 py-3 text-xs font-medium tracking-wider text-left text-gray-500 uppercase">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {searchedOwners.map((owner) => (
                    <tr key={owner.id}>
                      <td className="px-6 py-4 text-sm font-medium text-gray-900 whitespace-nowrap">
                        {owner.nic}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                        {owner.name}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                        {owner.email}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-500 whitespace-nowrap">
                        {owner.phone}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(
                            owner.status
                          )}`}
                        >
                          {owner.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 space-x-4 text-sm font-medium whitespace-nowrap">
                        <button
                          onClick={() => openEditModal(owner)}
                          className="text-indigo-600 hover:text-indigo-900"
                        >
                          Edit
                        </button>
                        {owner.status !== "active" ? (
                          <button
                            onClick={() => handleActivateOwner(owner.id)}
                            className="text-green-600 hover:text-green-900"
                          >
                            Activate
                          </button>
                        ) : (
                          <button
                            onClick={() => handleDeactivateOwner(owner.id)}
                            className="text-red-600 hover:text-red-900"
                          >
                            Deactivate
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                  {searchedOwners.length === 0 && (
                    <tr>
                      <td
                        colSpan="6"
                        className="px-6 py-4 text-center text-gray-500"
                      >
                        No owners found for this NIC
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      <Modal
        isOpen={isRegisterModalOpen}
        closeModal={() => setIsRegisterModalOpen(false)}
        title="Register New EV Owner"
      >
        <form onSubmit={handleCreateOwner} className="space-y-4">
          <div>
            <label
              htmlFor="Nic"
              className="block text-sm font-medium text-gray-700"
            >
              NIC Number
            </label>
            <input
              type="text"
              name="Nic"
              id="Nic"
              required
              className="input-field"
              value={newOwner.Nic}
              onChange={handleInputChange}
              placeholder="Enter NIC number"
            />
          </div>
          <div>
            <label
              htmlFor="Name"
              className="block text-sm font-medium text-gray-700"
            >
              Full Name
            </label>
            <input
              type="text"
              name="Name"
              id="Name"
              required
              className="input-field"
              value={newOwner.Name}
              onChange={handleInputChange}
              placeholder="Enter full name"
            />
          </div>
          <div>
            <label
              htmlFor="Phone"
              className="block text-sm font-medium text-gray-700"
            >
              Phone Number
            </label>
            <input
              type="tel"
              name="Phone"
              id="Phone"
              required
              className="input-field"
              value={newOwner.Phone}
              onChange={handleInputChange}
              placeholder="Enter phone number"
            />
          </div>
          <div>
            <label
              htmlFor="Email"
              className="block text-sm font-medium text-gray-700"
            >
              Email Address
            </label>
            <input
              type="email"
              name="Email"
              id="Email"
              required
              className="input-field"
              value={newOwner.Email}
              onChange={handleInputChange}
              placeholder="Enter email address"
            />
          </div>
          <div>
            <label
              htmlFor="Password"
              className="block text-sm font-medium text-gray-700"
            >
              Password
            </label>
            <input
              type="password"
              name="Password"
              id="Password"
              required
              className="input-field"
              value={newOwner.Password}
              onChange={handleInputChange}
              placeholder="Enter password (min 6 chars)"
            />
          </div>
          <div>
            <label
              htmlFor="ConfirmPassword"
              className="block text-sm font-medium text-gray-700"
            >
              Confirm Password
            </label>
            <input
              type="password"
              name="ConfirmPassword"
              id="ConfirmPassword"
              required
              className="input-field"
              value={newOwner.ConfirmPassword}
              onChange={handleInputChange}
              placeholder="Re-enter password"
            />
          </div>
          <div className="flex justify-end pt-4 space-x-3">
            <button
              type="button"
              onClick={() => setIsRegisterModalOpen(false)}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button type="submit" className="btn-primary">
              Register Owner
            </button>
          </div>
        </form>
      </Modal>

      {editingOwner && (
        <Modal
          isOpen={isEditModalOpen}
          closeModal={() => setIsEditModalOpen(false)}
          title="Edit EV Owner"
        >
          <form onSubmit={handleUpdateOwner} className="space-y-4">
            <div>
              <label
                htmlFor="editName"
                className="block text-sm font-medium text-gray-700"
              >
                Full Name
              </label>
              <input
                type="text"
                name="name"
                id="editName"
                required
                className="input-field"
                value={editingOwner.name}
                onChange={handleEditFormChange}
              />
            </div>
            <div>
              <label
                htmlFor="editEmail"
                className="block text-sm font-medium text-gray-700"
              >
                Email Address
              </label>
              <input
                type="email"
                name="email"
                id="editEmail"
                required
                className="input-field"
                value={editingOwner.email}
                onChange={handleEditFormChange}
              />
            </div>
            <div>
              <label
                htmlFor="editPhone"
                className="block text-sm font-medium text-gray-700"
              >
                Phone Number
              </label>
              <input
                type="tel"
                name="phone"
                id="editPhone"
                required
                className="input-field"
                value={editingOwner.phone}
                onChange={handleEditFormChange}
              />
            </div>
            <div className="flex justify-end pt-4 space-x-3">
              <button
                type="button"
                onClick={() => setIsEditModalOpen(false)}
                className="btn-secondary"
              >
                Cancel
              </button>
              <button type="submit" className="btn-primary">
                Save Changes
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};

export default EVOwners;
