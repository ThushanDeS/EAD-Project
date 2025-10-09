import { useState, useEffect } from 'react';

const StationForm = ({ onSubmit, onCancel, initialData = null, availableOperators = [] }) => {
    const [formData, setFormData] = useState({
        Name: '',
        Location: '',
        Type: 'AC',
        SlotCount: 1,
        OperatorId: '',
        Hours: { Open: '09:00', Close: '21:00' }
    });

    useEffect(() => {
        if (initialData) {
            setFormData({
                _id: initialData._id,
                Name: initialData.name || '',
                Location: initialData.lat && initialData.lng ? `${initialData.lat},${initialData.lng}` : (initialData.Location || ''),
                Type: initialData.type || 'AC',
                SlotCount: initialData.slotCount || 1,
                OperatorId: initialData.operatorId || '',
                Hours: initialData.Hours || { Open: '09:00', Close: '21:00' }
            });
        } else {
            setFormData({ Name: '', Location: '', Type: 'AC', SlotCount: 1, OperatorId: '', Hours: { Open: '09:00', Close: '21:00' } });
        }
    }, [initialData]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name === 'Open' || name === 'Close') {
            setFormData(prev => ({ ...prev, Hours: { ...prev.Hours, [name]: value } }));
        } else {
            setFormData(prev => ({ ...prev, [name]: value }));
        }
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div>
                <label htmlFor="Name" className="block text-sm font-medium text-gray-700">Station Name</label>
                <input id="Name" type="text" name="Name" required className="input-field" value={formData.Name} onChange={handleInputChange} placeholder="Enter station name" />
            </div>
            <div>
                <label htmlFor="Location" className="block text-sm font-medium text-gray-700">Location (Coordinates)</label>
                <input id="Location" type="text" name="Location" required className="input-field" value={formData.Location} onChange={handleInputChange} placeholder="e.g., 6.9271,79.8612" />
            </div>
            <div>
                <label htmlFor="OperatorId" className="block text-sm font-medium text-gray-700">Assign Operator</label>
                <select id="OperatorId" name="OperatorId" className="input-field" value={formData.OperatorId} onChange={handleInputChange}>
                    <option value="">Select an Operator</option>
                    {availableOperators.map(op => (<option key={op._id} value={op._id}>{op.name}</option>))}
                </select>
            </div>
            <div>
                <label htmlFor="SlotCount" className="block text-sm font-medium text-gray-700">Slot Count</label>
                <input id="SlotCount" type="number" name="SlotCount" min="1" required className="input-field" value={formData.SlotCount} onChange={handleInputChange} />
            </div>
            <div>
                <label htmlFor="Type" className="block text-sm font-medium text-gray-700">Station Type</label>
                <select id="Type" name="Type" className="input-field" value={formData.Type} onChange={handleInputChange}>
                    <option value="DC">DC Fast Charging</option>
                    <option value="AC">AC Charging</option>
                </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
                <div>
                    <label htmlFor="Open" className="block text-sm font-medium text-gray-700">Opening Time</label>
                    <input id="Open" type="time" name="Open" required className="input-field" value={formData.Hours.Open} onChange={handleInputChange} />
                </div>
                <div>
                    <label htmlFor="Close" className="block text-sm font-medium text-gray-700">Closing Time</label>
                    <input id="Close" type="time" name="Close" required className="input-field" value={formData.Hours.Close} onChange={handleInputChange} />
                </div>
            </div>
            <div className="flex justify-end pt-4 space-x-3">
                <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
                <button type="submit" className="btn-primary">{initialData ? 'Save Changes' : 'Create Station'}</button>
            </div>
        </form>
    );
};

export default StationForm;
