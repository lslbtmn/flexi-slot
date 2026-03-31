import { Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Login from './pages/Login'
import Register from './pages/Register'
import BusinessDashboard from './pages/BusinessDashboard'
import CustomerDashboard from './pages/CustomerDashboard'
import PublicServices from './pages/PublicServices'
import SlotPicker from './pages/SlotPicker'
import Home from './pages/Home'
import PaymentCallback from './pages/PaymentCallback'

function ProtectedRoute({ children, allowedRoles }) {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (!token) return <Navigate to="/login" replace />
  if (allowedRoles && !allowedRoles.includes(role)) return <Navigate to={role === 'CUSTOMER' ? '/customer' : role === 'BUSINESS_OWNER' ? '/business' : '/'} replace />
  return children
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="login" element={<Login />} />
        <Route path="register" element={<Register />} />
        <Route path="browse" element={<PublicServices />} />
        <Route path="business" element={<ProtectedRoute allowedRoles={['BUSINESS_OWNER', 'ADMIN']}><BusinessDashboard /></ProtectedRoute>} />
        <Route path="customer" element={<ProtectedRoute allowedRoles={['CUSTOMER']}><CustomerDashboard /></ProtectedRoute>} />
        <Route path="customer/slots/:serviceId" element={<SlotPicker />} />
        <Route path="payment/callback" element={<PaymentCallback />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
