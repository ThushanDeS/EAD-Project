import { useState, useEffect } from 'react';

const OperatorForm = ({ onSubmit, onCancel, initialData = null }) => {
    const [formData, setFormData] = useState({
        Name: '',
        Email: '',
        Nic: '',
        Phone: '',
        Password: '',
    });
    const [errors, setErrors] = useState({});

    useEffect(() => {
        if (initialData) {
            setFormData({
                Name: initialData.name || '',
                Email: initialData.email || '',
                Nic: initialData.nic || '',
                Phone: initialData.phone || '',
                Password: '',
            });
        } else {
            setFormData({ Name: '', Email: '', Nic: '', Phone: '', Password: '' });
        }
    }, [initialData]);

    const validate = () => {
        const newErrors = {};
        const nicRegex = /^(?:\d{12}|\d{9}[Vv])$/;  // ✅ NIC pattern

        if (!nicRegex.test(formData.Nic)) {
            newErrors.Nic = "NIC must be 12 digits (e.g., 197410320159) or 9 digits followed by 'V' or 'v'.";
        }

        if (!initialData && formData.Password.length < 6) {
            newErrors.Password = "Password must contain at least 6 characters.";
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
            <input type="text" name="Name" required className="input-field" value={formData.Name} onChange={handleInputChange} placeholder="Enter full name" />
            <input type="email" name="Email" required className="input-field" value={formData.Email} onChange={handleInputChange} placeholder="Enter email address" />
            <div>
                <input
                    type="text"
                    name="Nic"
                    required
                    className={`input-field ${errors.Nic ? 'border-red-500' : ''}`}
                    value={formData.Nic}
                    onChange={handleInputChange}
                    placeholder="Enter NIC number"
                />
                {errors.Nic && <p className="text-red-500 text-sm mt-1">{errors.Nic}</p>}
            </div>
            <input type="text" name="Phone" className="input-field" value={formData.Phone} onChange={handleInputChange} placeholder="Enter phone number (Optional)" />
            {!initialData && (
                <div>
                    <input
                        type="password"
                        name="Password"
                        required
                        className={`input-field ${errors.Password ? 'border-red-500' : ''}`}
                        value={formData.Password}
                        onChange={handleInputChange}
                        placeholder="Enter password"
                    />
                    {errors.Password && <p className="text-red-500 text-sm mt-1">{errors.Password}</p>}
                </div>
            )}
            <div className="flex justify-end space-x-3 pt-4">
                <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
                <button type="submit" className="btn-primary">{initialData ? 'Save Changes' : 'Create User'}</button>
            </div>
        </form>
    );
};

export default OperatorForm;
