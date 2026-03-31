import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import { slotsApi, bookingsApi, servicesApi } from '../api/client'

export default function SlotPicker() {
  const { serviceId } = useParams()
  const [slots, setSlots] = useState([])
  const [service, setService] = useState(null)
  const [loading, setLoading] = useState(true)
  const [booking, setBooking] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      try {
        const [sRes, slotsRes] = await Promise.all([
          servicesApi.getById(serviceId).catch(() => ({ data: null })),
          slotsApi.getByServiceId(serviceId, { page: 0, size: 50 }),
        ])
        if (sRes?.data) setService(sRes.data)
        // Group by Date conceptually, but for now we just show a rich grid
        setSlots(slotsRes.data?.content ?? [])
      } catch {
        setError('Failed to load slots')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [serviceId])

  const handleBook = async (slotId) => {
    setError('')
    const cid = localStorage.getItem('customerId')
    if (!cid) {
      setError('You must create your customer profile first before booking. Head to your dashboard.')
      return
    }
    try {
      const { data } = await bookingsApi.create({ slotId })
      setBooking(data)
      setSlots((prev) => prev.filter((s) => s.id !== slotId))
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed')
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="w-10 h-10 border-4 border-slate-200 border-t-brand-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  // Group slots by date
  const groupedSlots = slots.reduce((acc, slot) => {
    if (!acc[slot.slotDate]) acc[slot.slotDate] = []
    acc[slot.slotDate].push(slot)
    return acc
  }, {})

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-fade-in relative">
      <div className="bg-gradient-to-r from-slate-900 to-slate-800 rounded-3xl p-8 text-white shadow-xl flex flex-col md:flex-row justify-between items-start md:items-center">
        <div>
          <h1 className="text-3xl font-extrabold mb-1">Select a Time Slot</h1>
          {service && (
            <p className="text-slate-300 text-lg flex items-center">
              Booking for <span className="font-bold text-white ml-2">{service.serviceName}</span>
              <span className="mx-3 text-slate-500">•</span>
              <span className="text-brand-300 font-semibold">{service.durationMinutes} min</span>
            </p>
          )}
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 text-red-700 rounded-2xl flex items-center justify-between shadow-sm">
          <p className="font-medium">{error}</p>
          {error.includes('profile') && (
            <Link to="/customer" className="text-sm font-bold underline text-red-800">Go to Dashboard</Link>
          )}
        </div>
      )}

      {booking && (
        <div className="p-6 bg-green-50 border border-green-200 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm animate-fade-in">
          <div className="space-y-2">
            <div>
              <h3 className="text-green-800 font-bold text-lg leading-tight">Booking Confirmed!</h3>
              <p className="text-green-700/80 italic text-sm mt-0.5">ID: <span className="font-mono">{booking.id}</span></p>
            </div>
            
            <div className="bg-white/60 rounded-lg p-3 border border-green-100/50">
              <p className="text-sm font-bold text-green-900">{booking.serviceName} <span className="text-green-700 font-medium whitespace-nowrap">at {booking.businessName}</span></p>
              <div className="flex items-center gap-2 mt-1.5 text-xs text-green-800 font-medium">
                <svg className="w-4 h-4 opacity-70" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                <span>{booking.slotDate ? new Date(booking.slotDate + 'T00:00:00').toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' }) : ''} · {booking.startTime?.substring(0,5)} - {booking.endTime?.substring(0,5)}</span>
              </div>
            </div>
          </div>
          <Link to="/customer" className="px-6 py-3 bg-green-600 hover:bg-green-700 text-white rounded-xl font-bold transition-colors whitespace-nowrap shadow-sm">
            View in Dashboard
          </Link>
        </div>
      )}

      <div className="space-y-10">
        {Object.keys(groupedSlots).length === 0 ? (
          <div className="text-center py-20 bg-white rounded-3xl border border-dashed border-slate-300">
            <div className="text-6xl mb-4">🗓️</div>
            <h2 className="text-xl font-bold text-slate-800 mb-2">No slots available</h2>
            <p className="text-slate-500">There are no open slots for this service right now.</p>
          </div>
        ) : (
          Object.entries(groupedSlots).map(([date, dateSlots]) => (
            <div key={date} className="bg-white rounded-3xl p-8 shadow-sm border border-slate-100">
              <h2 className="text-2xl font-bold text-slate-800 mb-6 flex items-center">
                <svg className="w-6 h-6 mr-3 text-brand-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                {new Date(date).toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'short', day: 'numeric' })}
              </h2>
              
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
                {dateSlots.map((s) => {
                  const isHighDemand = service && s.price > service.basePrice
                  const isDiscounted = service && s.price < service.basePrice
                  
                  return (
                    <button 
                      key={s.id} 
                      onClick={() => handleBook(s.id)}
                      className={`group relative px-4 py-4 rounded-2xl border-2 hover:border-brand-500 text-left transition-all duration-200 focus:outline-none focus:ring-4 focus:ring-brand-100 ${
                        isHighDemand ? 'bg-orange-50 border-orange-100/50' :
                        isDiscounted ? 'bg-green-50 border-green-100/50' :
                        'bg-slate-50 border-slate-200 hover:bg-brand-50'
                      }`}
                    >
                      <div className={`text-slate-800 font-bold transition-colors ${!isHighDemand && !isDiscounted ? 'group-hover:text-brand-700' : ''}`}>
                        {s.startTime.substring(0,5)}
                      </div>
                      <div className="mt-1.5 flex items-center space-x-2">
                        <span className={`text-xs font-bold inline-block px-2 py-0.5 rounded ${
                          isHighDemand ? 'bg-orange-100 text-orange-800' :
                          isDiscounted ? 'bg-green-100 text-green-800' :
                          'bg-slate-200 text-slate-600 group-hover:bg-brand-100 group-hover:text-brand-700'
                        }`}>
                          ${s.price.toFixed(2)}
                        </span>
                        {(isHighDemand || isDiscounted) && (
                          <span className="text-[10px] text-slate-400 line-through font-semibold">
                            ${service.basePrice.toFixed(2)}
                          </span>
                        )}
                      </div>
                      
                      {isHighDemand && (
                        <div className="absolute -top-2 -right-2 bg-orange-500 text-white text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full shadow-sm animate-pulse flex items-center">
                          <span className="mr-1">🔥</span> Demand
                        </div>
                      )}
                      {isDiscounted && (
                        <div className="absolute -top-2 -right-2 bg-green-500 text-white text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded-full shadow-sm flex items-center">
                          <span className="mr-1">📉</span> SavE
                        </div>
                      )}

                      {/* Hover tooltip for end time */}
                      <div className="absolute opacity-0 group-hover:opacity-100 bg-slate-800 text-white text-xs font-bold py-1 px-2 rounded -bottom-8 left-1/2 transform -translate-x-1/2 transition-opacity pointer-events-none whitespace-nowrap z-10 shadow-md">
                        Until {s.endTime.substring(0,5)}
                      </div>
                    </button>
                  )
                })}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
