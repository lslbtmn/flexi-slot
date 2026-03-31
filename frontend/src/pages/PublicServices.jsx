import { useState, useEffect } from 'react'
import { Link, useSearchParams, useNavigate } from 'react-router-dom'
import api from '../api/client'

export default function PublicServices() {
  const [searchParams] = useSearchParams()
  const initialBusinessId = searchParams.get('businessId') || ''
  const navigate = useNavigate()
  const [business, setBusiness] = useState(null)
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (initialBusinessId) {
      loadBusinessAndServices(initialBusinessId)
    } else {
      navigate('/')
    }
  }, [initialBusinessId, navigate])

  const loadBusinessAndServices = async (id) => {
    if (!id?.trim()) return
    setLoading(true)
    setError('')
    try {
      const [bRes, sRes] = await Promise.all([
        api.get(`/business/${id}`),
        api.get(`/services/business/${id}`, { params: { page: 0, size: 50 } }),
      ])
      setBusiness(bRes.data)
      setServices(sRes.data?.content ?? [])
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load details')
      setBusiness(null)
      setServices([])
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8 animate-fade-in">
      <div className="flex items-center justify-between border-b border-slate-200 pb-5">
        <h1 className="text-3xl font-extrabold text-slate-800">Browse Services</h1>
        <Link to="/" className="text-brand-600 hover:text-brand-700 font-medium hover:underline text-sm flex items-center">
          &larr; Back to Home
        </Link>
      </div>

      {error && (
        <div className="p-4 bg-red-50 text-red-600 rounded-xl border border-red-100 font-medium">
          {error}
        </div>
      )}

      {loading && (
        <div className="flex justify-center py-12">
          <div className="w-8 h-8 rounded-full border-4 border-slate-200 border-t-brand-600 border-r-brand-600 animate-spin"></div>
        </div>
      )}

      {!loading && business && (
        <div className="bg-white rounded-3xl p-8 shadow-sm border border-slate-100 flex items-center space-x-6">
          <div className="w-16 h-16 rounded-full bg-brand-100 flex items-center justify-center text-brand-600 text-2xl font-bold">
            {business.name.charAt(0).toUpperCase()}
          </div>
          <div>
            <h2 className="text-2xl font-bold text-slate-900">{business.name}</h2>
            <div className="mt-1 flex items-center space-x-4 text-sm text-slate-500 font-medium">
              <span className="flex items-center"><span className="text-brand-500 mr-1.5">•</span> {business.serviceType}</span>
              <span>{business.email}</span>
            </div>
          </div>
        </div>
      )}

      {!loading && services.length > 0 && (
        <div className="space-y-6">
          <h3 className="text-xl font-bold text-slate-800">Available Offerings</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {services.map((s) => (
              <div key={s.id} className="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md border border-slate-100 transition-all group">
                <div className="flex justify-between items-start mb-4">
                  <h4 className="text-lg font-bold text-slate-900 group-hover:text-brand-600 transition-colors">{s.serviceName}</h4>
                  <span className="bg-green-100 text-green-700 text-sm font-bold px-3 py-1 rounded-full">${s.basePrice}</span>
                </div>
                <div className="text-slate-500 text-sm mb-6 flex items-center space-x-2">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                  <span>{s.durationMinutes} Minutes</span>
                </div>
                
                <Link 
                  to={`/customer/slots/${s.id}?businessId=${initialBusinessId}`} 
                  className="block w-full text-center py-2.5 bg-brand-50 hover:bg-brand-600 text-brand-700 hover:text-white rounded-xl font-semibold transition-colors"
                >
                  View Available Slots
                </Link>
              </div>
            ))}
          </div>
        </div>
      )}

      {!loading && !error && business && services.length === 0 && (
        <div className="py-16 text-center text-slate-500 bg-white rounded-3xl border border-dashed border-slate-300">
          <p className="text-lg">No services currently listed by this business.</p>
        </div>
      )}
    </div>
  )
}
