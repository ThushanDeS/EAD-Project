import { useState, useEffect } from 'react';
import { adminAPI } from '../api/admin';
import Modal from '../components/common/Modal';
import Pagination from '../components/common/Pagination';
import LoadingSpinner from '../components/common/LoadingSpinner';
import OperatorsTable from '../components/operators/OperatorsTable';
import OperatorForm from '../components/operators/OperatorForm';

const Operators = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const [itemsPerPage] = useState(8);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingUser, setEditingUser] = useState(null);

    const fetchUsers = async () => {
        setLoading(true);
        try {
            const data = await adminAPI.getAllOperators();
            setUsers(data || []);
        } catch (error) {
            console.error('Error fetching users:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const handleFormSubmit = async (formData) => {
        try {
            if (editingUser) {
                const payload = { Name: formData.Name, Email: formData.Email, Nic: formData.Nic, Phone: formData.Phone };
                await adminAPI.updateOperator(editingUser._id, payload);
            } else {
                await adminAPI.createUser(formData);
            }
            closeModal();
            fetchUsers();
        } catch (error) {
            if (error.response && error.response.status === 409) {
                alert(error.response.data.message);
            } else {
                console.error('Error submitting form:', error);
                alert('An unexpected error occurred. Please try again.');
            }
        }
    };

    const handleDeleteUser = async (userId) => {
        if (window.confirm('Are you sure you want to delete this operator?')) {
            try {
                await adminAPI.deleteOperator(userId);
                fetchUsers();
            } catch (error) {
                console.error('Error deleting user:', error);
            }
        }
    };

    const handleToggleStatus = async (user) => {
        const action = user.status === 'active' ? 'deactivate' : 'reactivate';
        if (window.confirm(`Are you sure you want to ${action} this operator?`)) {
            try {
                if (action === 'deactivate') {
                    await adminAPI.deactivateOperator(user._id);
                } else {
                    await adminAPI.reactivateOperator(user._id);
                }
                fetchUsers();
            } catch (error) {
                console.error(`Error ${action}ing user:`, error);
            }
        }
    };

    const openCreateModal = () => {
        setEditingUser(null);
        setIsModalOpen(true);
    };

    const openEditModal = (user) => {
        setEditingUser(user);
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingUser(null);
    };

    const indexOfLastItem = currentPage * itemsPerPage;
    const indexOfFirstItem = indexOfLastItem - itemsPerPage;
    const currentUsers = users.slice(indexOfFirstItem, indexOfLastItem);

    const paginate = (pageNumber) => setCurrentPage(pageNumber);

    if (loading) {
        return <LoadingSpinner />;
    }

    return (
        <div className="space-y-6">
            <div className="flex justify-end items-center">
                <button
                    onClick={openCreateModal}
                    className="btn-primary flex items-center"
                >
                    Add New Operator
                </button>
            </div>

            <div className="card">
                <OperatorsTable
                    users={currentUsers}
                    onEdit={openEditModal}
                    onToggleStatus={handleToggleStatus}
                    onDelete={handleDeleteUser}
                />
                <Pagination
                    itemsPerPage={itemsPerPage}
                    totalItems={users.length}
                    paginate={paginate}
                    currentPage={currentPage}
                />
            </div>

            <Modal isOpen={isModalOpen} closeModal={closeModal} title={editingUser ? "Edit Station Operator" : "Create New Station Operator"}>
                <OperatorForm
                    onSubmit={handleFormSubmit}
                    onCancel={closeModal}
                    initialData={editingUser}
                />
            </Modal>
        </div>
    );
};

export default Operators;
