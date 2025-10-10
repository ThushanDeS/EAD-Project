import { useState, useEffect } from 'react';

const EVOwnerForm = ({ onSubmit, onCancel, initialData = null }) => {
    const [formData, setFormData] = useState({
        Nic: '',
        Name: '',
        Phone: '',
        Email: ''
    });
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (initialData) {
            setFormData({
                Nic: initialData.nic || '',
                Name: initialData.name || '',
                Phone: initialData.phone || '',
                Email: initialData.email || ''
            });
        } else {
            setFormData({ Nic: '', Name: '', Phone: '', Email: '' });
        }
    }, [initialData]);

    const validate = () => {
        const newErrors = {};
        const nicRegex = /^(?:\d{12}|\d{9}[Vv])$/; // ✅ 12 digits OR 9 digits + V/v

        if (!nicRegex.test(formData.Nic)) {
            newErrors.Nic = "NIC must be 12 digits (e.g., 199902301234) or 9 digits followed by 'V' or 'v'.";
        }

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleInputChange = (e) => {
        setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
        setErrors(prev => ({ ...prev, [e.target.name]: '' }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!validate()) return;
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="Nic" className="block text-sm font-medium text-gray-700">
                    NIC Number
                </label>
                <input
                    type="text"
                    name="Nic"
                    id="Nic"
                    required
                    className={`input-field ${errors.Nic ? 'border-red-500' : ''}`}
                    value={formData.Nic}
                    onChange={handleInputChange}
                    placeholder="Enter NIC number"
                    disabled={!!initialData}
                />
                {errors.Nic && <p className="text-red-500 text-sm mt-1">{errors.Nic}</p>}
            </div>
            <div>
                <label htmlFor="Name" className="block text-sm font-medium text-gray-700">Full Name</label>
                <input type="text" name="Name" id="Name" required className="input-field" value={formData.Name} onChange={handleInputChange} placeholder="Enter full name" />
            </div>
            <div>
                <label htmlFor="Phone" className="block text-sm font-medium text-gray-700">Phone Number</label>
                <input type="tel" name="Phone" id="Phone" required className="input-field" value={formData.Phone} onChange={handleInputChange} placeholder="Enter phone number" />
            </div>
            <div>
                <label htmlFor="Email" className="block text-sm font-medium text-gray-700">Email Address</label>
                <input type="email" name="Email" id="Email" required className="input-field" value={formData.Email} onChange={handleInputChange} placeholder="Enter email address" />
            </div>
            <div className="flex justify-end space-x-3 pt-4">
                <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
                <button type="submit" className="btn-primary">{initialData ? 'Save Changes' : 'Register Owner'}</button>
            </div>
        </form>
    );
};

export default EVOwnerForm;
