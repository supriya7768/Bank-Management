
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import './App.css';

import Dashboard from './pages/Dashboard';
import Accounts from './pages/Accounts';
import Transactions from './pages/Transactions';
import Customers from './pages/Customers';

function App() {
  return (
    <BrowserRouter>
      <div className="app-container">
        <header className="header">
          <h1>Bank Management System</h1>

          <nav className="navbar">
            <Link to="/">Dashboard</Link>
            <Link to="/customers">Customers</Link>
            <Link to="/accounts">Accounts</Link>
            <Link to="/transactions">Transactions</Link>
          </nav>
        </header>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/accounts" element={<Accounts />} />
            <Route path="/transactions" element={<Transactions />} />
            <Route path="/customers" element={<Customers />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}

export default App;
