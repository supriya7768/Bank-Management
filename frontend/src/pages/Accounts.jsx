
import { useEffect, useState } from 'react';

function Accounts() {
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState({
    customerId: '',
    accountType: '',
  });
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  const [customers, setCustomers] = useState([]);
  const [loadingCustomers, setLoadingCustomers] = useState(true);

  const [accounts, setAccounts] = useState([]);
  const [loadingAccounts, setLoadingAccounts] = useState(true);
  const [accountsError, setAccountsError] = useState('');

  const fetchAccounts = async () => {
    try {
      setLoadingAccounts(true);
      setAccountsError('');

      const response = await fetch('http://localhost:8080/api/accounts');

      if (!response.ok) {
        throw new Error('Failed to fetch accounts');
      }

      const data = await response.json();
      setAccounts(data);
    } catch (error) {
      console.error('Error fetching accounts:', error);
      setAccountsError(error.message);
    } finally {
      setLoadingAccounts(false);
    }
  };

  const fetchCustomers = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/customers');

      if (!response.ok) {
        throw new Error('Failed to fetch customers');
      }

      const data = await response.json();
      setCustomers(data);
    } catch (error) {
      console.error('Error fetching customers:', error);
      alert('Unable to load customers. Please try again.');
    } finally {
      setLoadingCustomers(false);
    }
  };

  useEffect(() => {
    fetchAccounts();
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

    if (!formData.customerId || !formData.accountType) {
      alert('Please select a customer and account type');
      return;
    }

    try {
      const response = await fetch('http://localhost:8080/api/accounts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          customerId: Number(formData.customerId),
          accountType: formData.accountType,
        }),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Failed to create account');
      }

      const createdAccount = await response.json();

      alert(
        `Account created successfully!\nAccount Number: ${createdAccount.accountNumber}`
      );

      setFormData({
        customerId: '',
        accountType: '',
      });

      setShowForm(false);

      await fetchAccounts();
    } catch (error) {
      console.error('Error creating account:', error);
      alert(`Unable to create account: ${error.message}`);
    }
  };


  return (
    <div>
      <h2>Bank Accounts</h2>
      <p>View and manage customer bank accounts.</p>


      <input
        className="form-input"
        type="text"
        placeholder="Search by account number or customer ID..."
        value={searchTerm}
        onChange={(event) => setSearchTerm(event.target.value)}
      />


      <button
        className="primary-button"
        type="button"
        onClick={() => setShowForm(true)}
      >
        + Create Account
      </button>

      {showForm && (
        <div className="create-account-panel">
          <h3>Create New Account</h3>

          <form className="account-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Customer</label>

              <select
                className="form-input"
                name="customerId"
                value={formData.customerId}
                onChange={handleInputChange}
                required
                disabled={loadingCustomers}
              >
                <option value="">
                  {loadingCustomers
                    ? 'Loading customers...'
                    : 'Select a customer'}
                </option>

                {customers.map((customer) => (
                  <option key={customer.id} value={customer.id}>
                    {customer.name} (ID: {customer.id})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Account Type</label>

              <select
                className="form-input"
                name="accountType"
                value={formData.accountType}
                onChange={handleInputChange}
                required
              >
                <option value="">Select account type</option>
                <option value="SAVINGS">Savings</option>
                <option value="CURRENT">Current</option>
              </select>
            </div>

            <button
              type="submit"
              className="primary-button"
              disabled={loadingCustomers}
            >
              Create Account
            </button>
          </form>

          <button
            type="button"
            className="action-button"
            onClick={() => {
              setShowForm(false);
              setFormData({
                customerId: '',
                accountType: '',
              });
            }}
          >
            Cancel
          </button>
        </div>
      )}


      {selectedAccount && (
        <div className="account-form">
          <h3>Account Details</h3>

          <p>
            <strong>Account Number:</strong>{' '}
            {selectedAccount.accountNumber}
          </p>

          <p>
            <strong>Customer ID:</strong>{' '}
            {selectedAccount.customerId}
          </p>

          <p>
            <strong>Account Type:</strong>{' '}
            {selectedAccount.accountType}
          </p>

          <p>
            <strong>Balance:</strong>{' '}
            ₹{Number(selectedAccount.balance).toLocaleString('en-IN')}
          </p>

          <p>
            <strong>Status:</strong> {selectedAccount.status}
          </p>

          <button
            type="button"
            className="action-button"
            onClick={() => setSelectedAccount(null)}
          >
            Close Details
          </button>
        </div>
      )}


      {loadingAccounts && <p>Loading accounts...</p>}

      {accountsError && (
        <p style={{ color: 'red' }}>{accountsError}</p>
      )}

      {!loadingAccounts && !accountsError && (
        <div className="table-container">
          <table className="accounts-table">
            <thead>
              <tr>
                <th>Account Number</th>
                <th>Account Type</th>
                <th>Balance</th>
                <th>Status</th>
                <th>Customer ID</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              
{accounts
  .filter((account) => {
    const search = searchTerm.toLowerCase().trim();

    return (
      account.accountNumber?.toLowerCase().includes(search) ||
      String(account.customerId).includes(search)
    );
  })
  .map((account) => (

                <tr key={account.id}>
                  <td>{account.accountNumber}</td>
                  <td>{account.accountType}</td>
                  <td>
                    ₹{Number(account.balance).toLocaleString('en-IN')}
                  </td>
                  <td>{account.status}</td>
                  <td>{account.customerId}</td>
                  <td>
                    <button
                      className="action-button"
                      type="button"
                      onClick={() => setSelectedAccount(account)}
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default Accounts;
