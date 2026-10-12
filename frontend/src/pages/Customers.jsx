import { useEffect, useState } from 'react';

function Customers() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    address: '',
    phone: '',
  });

const [customers, setCustomers] = useState([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState('');

const fetchCustomers = async () => {
  try {
    setLoading(true);
    setError('');

    const response = await fetch('http://localhost:8080/api/customers');

    if (!response.ok) {
      throw new Error('Failed to fetch customers');
    }

    const data = await response.json();
    setCustomers(data);
  } catch (error) {
    console.error('Error fetching customers:', error);
    setError(error.message);
  } finally {
    setLoading(false);
  }
};

useEffect(() => {
  fetchCustomers();
}, []);

  const handleInputChange = (event) => {
    const { name, value } = event.target;

    setFormData((previousData) => ({
      ...previousData,
      [name]: value,
    }));
  };

 
const handleSubmit = async (event) => {
  event.preventDefault();

  try {
    const response = await fetch('http://localhost:8080/api/customers', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(formData),
    });

    if (!response.ok) {
      const errorMessage = await response.text();
      throw new Error(errorMessage || 'Failed to create customer');
    }

    const customer = await response.json();

    alert(`Customer created successfully! ID: ${customer.id}`);

    setFormData({
      name: '',
      email: '',
      address: '',
      phone: '',
    });
  } catch (error) {
    console.error('Error creating customer:', error);
    alert(`Could not create customer: ${error.message}`);
  }
};

  return (
    <div>
      <h2>Customers</h2>
      <p>Create and manage bank customers.</p>

      <form className="account-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label className="form-label">Full Name</label>
          <input
            className="form-input"
            type="text"
            name="name"
            placeholder="Enter full name"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Email</label>
          <input
            className="form-input"
            type="email"
            name="email"
            placeholder="Enter email address"
            value={formData.email}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Address</label>
          <input
            className="form-input"
            type="text"
            name="address"
            placeholder="Enter address"
            value={formData.address}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Phone Number</label>
          <input
            className="form-input"
            type="tel"
            name="phone"
            placeholder="Enter 10-digit phone number"
            value={formData.phone}
            onChange={handleInputChange}
            pattern="[0-9]{10}"
            maxLength={10}
            required
          />
        </div>

        <button className="primary-button" type="submit">
          Create Customer
        </button>
      </form>

      <div className="table-container">
  <h3>Customer List</h3>

  {loading && <p>Loading customers...</p>}

  {error && <p>{error}</p>}

  {!loading && !error && (
    <table className="accounts-table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Name</th>
          <th>Email</th>
          <th>Phone</th>
          <th>Address</th>
        </tr>
      </thead>

      <tbody>
        {customers.map((customer) => (
          <tr key={customer.id}>
            <td>{customer.id}</td>
            <td>{customer.name}</td>
            <td>{customer.email}</td>
            <td>{customer.phone}</td>
            <td>{customer.address}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )}
</div>
    </div>

    
  );
}

export default Customers;
