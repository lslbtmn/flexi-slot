import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { customersApi, bookingsApi, paymentsApi } from '../api/client'

export default function CustomerDashboard() {
  const [customer, setCustomer] = useState(null)
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [form, setForm] = useState({ name: '', email: '', phone: '' })

  useEffect(() => {
    loadProfile()
  }, [])

  useEffect(() => {
    if (customer?.id) {
      localStorage.setItem('customerId', customer.id)
      loadBookings()
    }
  }, [customer?.id])

  const loadProfile = async () => {
    setError('')
    try {
      const { data } = await customersApi.getMe()
      setCustomer(data)
      setForm({ name: data.name, email: data.email, phone: data.phone || '' })
    } catch {
      setCustomer(null)
    } finally {
      setLoading(false)
    }
  }

  const loadBookings = () => {
    if (!customer?.id) return
    bookingsApi.getByCustomerId(customer.id, { page: 0, size: 50 })
      .then((r) => setBookings(r.data?.content ?? []))
      .catch(() => setBookings([]))
  }

  const handleCreateProfile = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const { data } = await customersApi.create(form)
      setCustomer(data)
      localStorage.setItem('customerId', data.id)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleUpdateProfile = async (e) => {
    e.preventDefault()
    if (!customer?.id) return
    setError('')
    try {
      const { data } = await customersApi.update(customer.id, form)
      setCustomer(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleCancel = async (bookingId) => {
    if(!window.confirm('Are you sure you want to cancel this booking?')) return
    setError('')
    try {
      await bookingsApi.cancel(bookingId)
      loadBookings()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handlePay = async (booking) => {
    setError('')
    try {
      const payload = {
        bookingId: booking.id,
        amount: booking.price,
        provider: 'PAYSTACK'
      }
      const { data } = await paymentsApi.initiate(payload)
      if (data.authorizationUrl) {
        window.location.href = data.authorizationUrl
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Payment initiation failed')
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="w-10 h-10 border-4 border-slate-200 border-t-brand-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (!customer) {
    return (
      <div className="max-w-2xl mx-auto mt-10 animate-fade-in">
        <div className="bg-white rounded-3xl p-8 shadow-lg border border-slate-100">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-slate-800">Complete Your Profile</h1>
            <p className="text-slate-500 mt-2">Finish setting up your profile to start booking appointments.</p>
          </div>
          
          {error && <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-xl font-medium">{error}</div>}
          
          <form onSubmit={handleCreateProfile} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Full Name</label>
                <input value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} required className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 focus:outline-none" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))} required className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 focus:outline-none" />
              </div>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-semibold text-slate-700">Phone Number (Optional)</label>
              <input value={form.phone} onChange={(e) => setForm((p) => ({ ...p, phone: e.target.value }))} className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 focus:outline-none" />
            </div>
            <button type="submit" className="w-full py-4 text-white bg-brand-600 hover:bg-brand-700 rounded-xl font-bold uppercase tracking-wide shadow-md transition-all transform hover:-translate-y-0.5">
              Save Profile
            </button>
          </form>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto space-y-10 animate-fade-in">
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center border-b border-slate-200 pb-6 gap-4">
        <div>
          <h1 className="text-4xl font-extrabold text-slate-900">My Dashboard</h1>
          <p className="text-slate-500 text-lg mt-1">Manage your bookings and personal details.</p>
        </div>
        <Link to="/browse" className="px-6 py-3 bg-slate-900 hover:bg-slate-800 text-white rounded-xl font-semibold shadow transition-all transform hover:scale-105">
          + Book New Service
        </Link>
      </div>

      {error && <div className="p-4 bg-red-50 text-red-600 rounded-xl font-medium border border-red-100">{error}</div>}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Profile Section */}
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white rounded-3xl p-6 shadow-sm border border-slate-100 sticky top-24">
            <h2 className="text-xl font-bold text-slate-800 mb-6 flex items-center">
              <svg className="w-5 h-5 mr-2 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
              Profile Settings
            </h2>
            <form onSubmit={handleUpdateProfile} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Name</label>
                <input value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none transition-colors" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none transition-colors" />
              </div>
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Phone</label>
                <input value={form.phone} onChange={(e) => setForm((p) => ({ ...p, phone: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none transition-colors" />
              </div>
              <button type="submit" className="w-full mt-4 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-lg font-semibold transition-colors">
                Update Profile
              </button>
            </form>
          </div>
        </div>

        {/* Bookings Section */}
        <div className="lg:col-span-2 space-y-6">
          <h2 className="text-2xl font-bold text-slate-800">Your Bookings</h2>
          
          <div className="space-y-4">
            {bookings.length === 0 ? (
              <div className="bg-white rounded-3xl p-12 text-center border border-slate-100 shadow-sm">
                <div className="w-16 h-16 bg-brand-50 rounded-full flex items-center justify-center mx-auto mb-4 text-brand-500">
                  <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                </div>
                <h3 className="text-lg font-bold text-slate-900 mb-2">No bookings yet</h3>
                <p className="text-slate-500 mb-6">You haven't made any appointments. Find a service and book your first slot today!</p>
                <Link to="/browse" className="inline-block px-6 py-2.5 bg-brand-50 text-brand-700 font-semibold rounded-full hover:bg-brand-100 transition-colors">
                  Browse Services
                </Link>
              </div>
            ) : (
              bookings.map((b) => (
                <div key={b.id} className="bg-white rounded-2xl p-6 shadow-sm hover:shadow-md border border-slate-100 transition-all flex flex-col sm:flex-row justify-between gap-6">
                  <div className="space-y-3 flex-grow">
                    <div className="flex items-center space-x-3">
                      <span className="font-mono text-xs text-slate-400 font-semibold bg-slate-100 px-2 py-1 rounded">#{b.id.substring(0,8)}</span>
                      
                      <span className={`text-xs font-bold px-3 py-1 rounded-full ${
                        b.bookingStatus === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
                        b.bookingStatus === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                        'bg-yellow-100 text-yellow-700'
                      }`}>
                        {b.bookingStatus}
                      </span>

                      <span className={`text-xs font-bold px-3 py-1 rounded-full border ${
                        b.paymentStatus === 'SUCCESS' ? 'border-green-200 text-green-700 bg-white' :
                        b.paymentStatus === 'FAILED' ? 'border-red-200 text-red-700 bg-white' :
                        'border-slate-200 text-slate-600 bg-white'
                      }`}>
                        {b.paymentStatus === 'SUCCESS' ? 'Paid' : 'Unpaid'}
                      </span>
                    </div>
                    
                    <div className="mt-3 bg-slate-50 rounded-xl p-4 border border-slate-100 flex flex-col gap-2">
                      <div className="flex items-start gap-2">
                        <svg className="w-5 h-5 text-brand-500 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path></svg>
                        <div>
                          <p className="text-sm font-bold text-slate-800">{b.serviceName || 'Service Name Unavailable'}</p>
                          <p className="text-xs text-slate-500 font-medium">at {b.businessName || 'Business Name Unavailable'}</p>
                        </div>
                      </div>
                      
                      <div className="flex items-start gap-2 mt-1">
                        <svg className="w-5 h-5 text-slate-400 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                        <p className="text-sm text-slate-700 font-semibold mt-0.5">
                          {b.slotDate ? `${new Date(b.slotDate + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })} · ${b.startTime?.substring(0,5)} - ${b.endTime?.substring(0,5)}` : 'Date/Time Unavailable'}
                        </p>
                      </div>

                      {b.location && (
                        <div className="flex items-start gap-2 mt-1">
                          <svg className="w-5 h-5 text-slate-400 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.243-4.243a8 8 0 1111.314 0z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                          <p className="text-sm text-slate-600 mt-0.5">{b.location}</p>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex flex-col gap-2 min-w-[140px] justify-between border-t sm:border-t-0 sm:border-l border-slate-100 pt-4 sm:pt-0 sm:pl-6">
                    <div>
                      <p className="text-sm text-slate-500 font-medium">Total Amount</p>
                      <h4 className="text-2xl font-black text-brand-600 mt-0.5">${b.price}</h4>
                    </div>
                    
                    <div className="space-y-2 mt-4 sm:mt-0">
                    {b.bookingStatus === 'CONFIRMED' && b.paymentStatus !== 'SUCCESS' && (
                      <button onClick={() => handlePay(b)} className="w-full py-2 bg-brand-600 hover:bg-brand-700 text-white font-semibold rounded-lg shadow transition-colors text-sm text-center">
                        Pay Now
                      </button>
                    )}
                    {b.bookingStatus === 'CONFIRMED' && (
                      <button onClick={() => handleCancel(b.id)} className="w-full py-2 bg-white border border-red-200 hover:bg-red-50 text-red-600 font-semibold rounded-lg transition-colors text-sm text-center">
                        Cancel Appt
                      </button>
                    )}
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
