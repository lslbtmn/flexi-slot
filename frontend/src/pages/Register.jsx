import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { auth } from '../api/client'

export default function Register() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole] = useState('CUSTOMER')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { data } = await auth.register({ email, password, role })
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('role', data.role)
      if (data.role === 'BUSINESS_OWNER') navigate('/business')
      else if (data.role === 'CUSTOMER') navigate('/customer')
      else navigate('/business')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)]">
      <div className="w-full max-w-md p-8 bg-white rounded-3xl shadow-xl border border-slate-100 transform transition-all">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-slate-800">Create Account</h1>
          <p className="text-slate-500 mt-2">Join FlexiSlot to book or provide services</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="space-y-2">
            <label className="text-sm font-semibold text-slate-700 block">Email</label>
            <input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              required 
              className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all"
              placeholder="you@example.com"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-semibold text-slate-700 block">Password</label>
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              minLength={6} 
              required 
              className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent transition-all"
              placeholder="Min. 6 characters"
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-semibold text-slate-700 block">I am a...</label>
            <select 
              value={role} 
              onChange={(e) => setRole(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500 focus:border-transparent bg-white transition-all"
            >
              <option value="CUSTOMER">Customer (Looking to book)</option>
              <option value="BUSINESS_OWNER">Business Owner (Offering services)</option>
            </select>
          </div>

          {error && (
            <div className="p-3 bg-red-50 text-red-600 rounded-lg text-sm text-center border border-red-100">
              {error}
            </div>
          )}

          <button 
            type="submit" 
            disabled={loading}
            className="w-full py-3 px-4 bg-brand-600 hover:bg-brand-700 text-white rounded-xl font-bold shadow-md hover:shadow-lg transition-all transform hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed mt-4"
          >
            {loading ? 'Registering...' : 'Create Account'}
          </button>
        </form>

        <p className="mt-8 text-center text-slate-600">
          Already have an account?{' '}
          <Link to="/login" className="text-brand-600 font-semibold hover:underline">
            Sign In
          </Link>
        </p>
      </div>
    </div>
  )
}
