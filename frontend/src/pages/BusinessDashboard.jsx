import { useState, useEffect } from 'react'
import { businessApi, servicesApi, slotsApi } from '../api/client'

export default function BusinessDashboard() {
  const [business, setBusiness] = useState(null)
  const [services, setServices] = useState([])
  const [slots, setSlots] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [tab, setTab] = useState('profile')

  const [formProfile, setFormProfile] = useState({ name: '', email: '', phone: '', location: '', serviceType: '', operatingHours: '' })
  const [formService, setFormService] = useState({ serviceName: '', basePrice: '', durationMinutes: '', dynamicPricingEnabled: true })
  const [formSlot, setFormSlot] = useState({ serviceId: '', slotDate: '', startTime: '', endTime: '' })
  const [formBulkSlot, setFormBulkSlot] = useState({ serviceId: '', slotDate: '' })

  const [editingServiceId, setEditingServiceId] = useState(null)
  const [editForm, setEditForm] = useState({ serviceName: '', basePrice: '', durationMinutes: '', dynamicPricingEnabled: true })

  useEffect(() => {
    loadBusiness()
  }, [])

  const loadBusiness = async () => {
    setError('')
    try {
      const { data } = await businessApi.getMe()
      setBusiness(data)
    } catch {
      setBusiness(null)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!business?.id) return
    servicesApi.getByBusinessId(business.id, { page: 0, size: 100 }).then((r) => setServices(r.data?.content ?? [])).catch(() => setServices([]))
  }, [business?.id])

  const loadSlotsForService = async (serviceId) => {
    if (!serviceId) return
    const res = await slotsApi.getByServiceId(serviceId, { page: 0, size: 100 })
    setSlots(res.data?.content ?? [])
  }

  const handleCreateBusiness = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const { data } = await businessApi.create(formProfile)
      setBusiness(data)
      setTab('profile')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleUpdateBusiness = async (e) => {
    e.preventDefault()
    if (!business?.id) return
    setError('')
    try {
      const { data } = await businessApi.update(business.id, formProfile)
      setBusiness(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleCreateService = async (e) => {
    e.preventDefault()
    setError('')
    try {
      const { data } = await servicesApi.create({
        serviceName: formService.serviceName,
        basePrice: parseFloat(formService.basePrice),
        durationMinutes: parseInt(formService.durationMinutes, 10),
        dynamicPricingEnabled: formService.dynamicPricingEnabled,
      })
      setServices((prev) => [...prev, data])
      setFormService({ serviceName: '', basePrice: '', durationMinutes: '', dynamicPricingEnabled: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleDeleteService = async (id) => {
    if (!window.confirm('Are you sure you want to delete this service? All slots and bookings tied to it will also be deleted!')) return
    setError('')
    try {
      await servicesApi.delete(id)
      setServices((prev) => prev.filter(s => s.id !== id))
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to delete service')
    }
  }

  const handleUpdateService = async (e, id) => {
    e.preventDefault()
    setError('')
    try {
      const { data } = await servicesApi.update(id, {
        serviceName: editForm.serviceName,
        basePrice: parseFloat(editForm.basePrice),
        durationMinutes: parseInt(editForm.durationMinutes, 10),
        dynamicPricingEnabled: editForm.dynamicPricingEnabled,
      })
      setServices((prev) => prev.map(s => (s.id === id ? data : s)))
      setEditingServiceId(null)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update service')
    }
  }

  const handleCreateSlot = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await slotsApi.create({
        serviceId: formSlot.serviceId,
        slotDate: formSlot.slotDate,
        startTime: formSlot.startTime,
        endTime: formSlot.endTime,
      })
      loadSlotsForService(formSlot.serviceId)
      setFormSlot({ ...formSlot, slotDate: '', startTime: '', endTime: '' })
    } catch (err) {
      setError(err.response?.data?.message || 'Failed')
    }
  }

  const handleGenerateBulk = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await slotsApi.generateBulk({
        serviceId: formBulkSlot.serviceId,
        slotDate: formBulkSlot.slotDate,
      })
      loadSlotsForService(formBulkSlot.serviceId)
      setFormBulkSlot({ ...formBulkSlot, slotDate: '' })
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to auto-generate slots')
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="w-10 h-10 border-4 border-slate-200 border-t-brand-600 rounded-full animate-spin"></div>
      </div>
    )
  }

  if (!business) {
    return (
      <div className="max-w-2xl mx-auto mt-10 animate-fade-in">
        <div className="bg-white rounded-3xl p-8 shadow-lg border border-slate-100">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-slate-800">Set Up Your Business</h1>
            <p className="text-slate-500 mt-2">Provide your business details below to start offering services.</p>
          </div>
          {error && <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-xl font-medium">{error}</div>}
          <form onSubmit={handleCreateBusiness} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Business Name</label>
                <input value={formProfile.name} onChange={(e) => setFormProfile((p) => ({ ...p, name: e.target.value }))} required className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="Acme Corp" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Contact Email</label>
                <input type="email" value={formProfile.email} onChange={(e) => setFormProfile((p) => ({ ...p, email: e.target.value }))} required className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="contact@acme.com" />
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Phone</label>
                <input value={formProfile.phone} onChange={(e) => setFormProfile((p) => ({ ...p, phone: e.target.value }))} className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="+1 234 567 8900" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Location</label>
                <input value={formProfile.location} onChange={(e) => setFormProfile((p) => ({ ...p, location: e.target.value }))} className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="123 Main St, City" />
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Service Category</label>
                <input value={formProfile.serviceType} onChange={(e) => setFormProfile((p) => ({ ...p, serviceType: e.target.value }))} className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="e.g. Hair Salon, Consulting" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-semibold text-slate-700">Operating Hours</label>
                <input value={formProfile.operatingHours} onChange={(e) => setFormProfile((p) => ({ ...p, operatingHours: e.target.value }))} required pattern="^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$" className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:ring-2 focus:ring-brand-500 outline-none" placeholder="09:00-17:00" />
                <p className="text-[11px] text-slate-500 mt-1 font-medium">Required format: 24-hr time HH:mm-HH:mm. Required to auto-generate slots.</p>
              </div>
            </div>
            <button type="submit" className="w-full py-4 text-white bg-brand-600 hover:bg-brand-700 rounded-xl font-bold uppercase tracking-wide shadow-md transition-all">
              Launch Business Profile
            </button>
          </form>
        </div>
      </div>
    )
  }

  const TabButton = ({ name, label, icon }) => (
    <button 
      onClick={() => setTab(name)}
      className={`flex items-center space-x-2 px-5 py-3 rounded-xl font-bold text-sm transition-all ${tab === name ? 'bg-slate-900 text-white shadow-md' : 'bg-white text-slate-600 hover:bg-slate-100'}`}
    >
      <span>{icon}</span>
      <span>{label}</span>
    </button>
  )

  return (
    <div className="max-w-6xl mx-auto animate-fade-in pb-12">
      <div className="mb-8">
        <h1 className="text-4xl font-extrabold text-slate-900">{business.name} portal</h1>
        <div className="mt-2 flex items-center space-x-4">
          <p className="text-slate-500 text-lg">Manage scheduling and offerings.</p>
          <span className="bg-brand-100 text-brand-700 text-xs font-mono px-3 py-1 rounded-full font-bold">ID: {business.id.substring(0,8)}</span>
        </div>
      </div>

      <div className="flex space-x-3 mb-8 overflow-x-auto pb-2">
        <TabButton name="profile" label="Profile Settings" icon="🏢" />
        <TabButton name="services" label="Service Offerings" icon="📦" />
        <TabButton name="slots" label="Slot Management" icon="🗓️" />
      </div>

      {error && <div className="p-4 mb-6 bg-red-50 text-red-600 rounded-xl font-medium border border-red-100">{error}</div>}

      {/* TABS CONTENT */}
      <div className="bg-white rounded-3xl p-8 shadow-sm border border-slate-100 min-h-[500px]">
        {tab === 'profile' && (
          <div className="max-w-3xl animate-fade-in space-y-6">
            <h2 className="text-2xl font-bold text-slate-800 border-b border-slate-100 pb-4">Business Information</h2>
            <form onSubmit={handleUpdateBusiness} className="space-y-5">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Name</label>
                  <input value={formProfile.name || business.name} onChange={(e) => setFormProfile((p) => ({ ...p, name: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Email</label>
                  <input type="email" value={formProfile.email || business.email} onChange={(e) => setFormProfile((p) => ({ ...p, email: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Phone</label>
                  <input value={formProfile.phone ?? business.phone} onChange={(e) => setFormProfile((p) => ({ ...p, phone: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Location</label>
                  <input value={formProfile.location ?? business.location} onChange={(e) => setFormProfile((p) => ({ ...p, location: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Service Category</label>
                  <input value={formProfile.serviceType ?? business.serviceType} onChange={(e) => setFormProfile((p) => ({ ...p, serviceType: e.target.value }))} className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Operating Hours</label>
                  <input value={formProfile.operatingHours ?? business.operatingHours ?? ''} onChange={(e) => setFormProfile((p) => ({ ...p, operatingHours: e.target.value }))} required pattern="^([0-1]?[0-9]|2[0-3]):[0-5][0-9]-([0-1]?[0-9]|2[0-3]):[0-5][0-9]$" placeholder="09:00-17:00" className="w-full px-4 py-2 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" />
                  <p className="text-[10px] text-slate-500 mt-1">Required format: 24-hour time HH:mm-HH:mm. Critical for auto-generating slots.</p>
                </div>
              </div>
              <div className="pt-4">
                <button type="submit" className="px-8 py-3 bg-slate-900 text-white rounded-xl font-bold hover:bg-slate-800 transition-colors">
                  Save Changes
                </button>
              </div>
            </form>
          </div>
        )}

        {tab === 'services' && (
          <div className="animate-fade-in grid grid-cols-1 lg:grid-cols-3 gap-10">
            <div className="lg:col-span-1 border-r border-slate-100 pr-0 lg:pr-10">
              <h2 className="text-xl font-bold text-slate-800 mb-6">Create New Service</h2>
              <form onSubmit={handleCreateService} className="space-y-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Service Name</label>
                  <input value={formService.serviceName} onChange={(e) => setFormService((s) => ({ ...s, serviceName: e.target.value }))} required className="w-full px-4 py-2.5 bg-slate-50 rounded-xl border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" placeholder="E.g. Men's Haircut" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Base Price ($)</label>
                  <input type="number" step="0.01" value={formService.basePrice} onChange={(e) => setFormService((s) => ({ ...s, basePrice: e.target.value }))} required className="w-full px-4 py-2.5 bg-slate-50 rounded-xl border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" placeholder="25.00" />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-500 uppercase">Duration (Minutes)</label>
                  <input type="number" value={formService.durationMinutes} onChange={(e) => setFormService((s) => ({ ...s, durationMinutes: e.target.value }))} required className="w-full px-4 py-2.5 bg-slate-50 rounded-xl border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none" placeholder="30" />
                </div>
                <div className="pt-2 flex items-center space-x-3">
                  <input type="checkbox" id="dynamicPricing" checked={formService.dynamicPricingEnabled} onChange={(e) => setFormService(s => ({...s, dynamicPricingEnabled: e.target.checked}))} className="w-5 h-5 text-brand-600 bg-slate-100 border-slate-300 rounded focus:ring-brand-500" />
                  <div className="flex flex-col">
                    <label htmlFor="dynamicPricing" className="text-sm font-bold text-slate-800 cursor-pointer">Enable Dynamic Pricing</label>
                    <p className="text-xs text-slate-500 max-w-xs">Prices will automatically drop during slow periods to encourage bookings, and increase during busy periods to maximize revenue.</p>
                  </div>
                </div>
                <button type="submit" className="w-full py-3 mt-4 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-colors shadow-sm text-sm">
                  Add Service
                </button>
              </form>
            </div>

            <div className="lg:col-span-2">
              <h2 className="text-xl font-bold text-slate-800 mb-6">Active Services</h2>
              {services.length === 0 ? (
                <div className="text-center py-10 bg-slate-50 rounded-2xl border border-dashed border-slate-300">
                  <p className="text-slate-500 font-medium">You haven't added any services yet.</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {services.map((s) => (
                    <div key={s.id} className="p-5 bg-white border border-slate-200 rounded-2xl shadow-sm hover:shadow-md transition-shadow relative overflow-hidden group">
                      {editingServiceId === s.id ? (
                        <form onSubmit={(e) => handleUpdateService(e, s.id)} className="space-y-3 relative z-10 bg-white">
                          <div className="space-y-1">
                            <label className="text-xs font-bold text-slate-500 uppercase">Service Name</label>
                            <input value={editForm.serviceName} onChange={(e) => setEditForm(prev => ({ ...prev, serviceName: e.target.value }))} required className="w-full px-3 py-1.5 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none text-sm" />
                          </div>
                          <div className="grid grid-cols-2 gap-2">
                            <div className="space-y-1">
                              <label className="text-xs font-bold text-slate-500 uppercase">Price ($)</label>
                              <input type="number" step="0.01" value={editForm.basePrice} onChange={(e) => setEditForm(prev => ({ ...prev, basePrice: e.target.value }))} required className="w-full px-3 py-1.5 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none text-sm" />
                            </div>
                            <div className="space-y-1">
                              <label className="text-xs font-bold text-slate-500 uppercase">Duration (Min)</label>
                              <input type="number" value={editForm.durationMinutes} onChange={(e) => setEditForm(prev => ({ ...prev, durationMinutes: e.target.value }))} required className="w-full px-3 py-1.5 bg-slate-50 rounded-lg border border-slate-200 focus:bg-white focus:ring-2 focus:ring-brand-500 outline-none text-sm" />
                            </div>
                          </div>
                          <div className="pt-1 flex items-center space-x-2">
                            <input type="checkbox" id={`dynamicPricing-${s.id}`} checked={editForm.dynamicPricingEnabled} onChange={(e) => setEditForm(prev => ({...prev, dynamicPricingEnabled: e.target.checked}))} className="w-4 h-4 text-brand-600 bg-slate-100 border-slate-300 rounded focus:ring-brand-500" />
                            <label htmlFor={`dynamicPricing-${s.id}`} className="text-xs font-bold text-slate-700 cursor-pointer">Dynamic Pricing</label>
                          </div>
                          <div className="flex space-x-2 pt-2">
                            <button type="submit" className="flex-1 py-1.5 bg-brand-600 text-white rounded-lg font-bold hover:bg-brand-700 text-xs transition-colors">Save</button>
                            <button type="button" onClick={() => setEditingServiceId(null)} className="flex-1 py-1.5 bg-slate-200 text-slate-700 rounded-lg font-bold hover:bg-slate-300 text-xs transition-colors">Cancel</button>
                          </div>
                        </form>
                      ) : (
                        <>
                          <div className="absolute top-3 right-3 flex space-x-1.5 opacity-0 group-hover:opacity-100 transition-opacity z-20">
                            <button onClick={() => { setEditingServiceId(s.id); setEditForm({ serviceName: s.serviceName, basePrice: s.basePrice, durationMinutes: s.durationMinutes, dynamicPricingEnabled: s.dynamicPricingEnabled }) }} className="p-1.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-md flex items-center justify-center transition-colors shadow-sm" title="Edit Service">
                              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" /></svg>
                            </button>
                            <button onClick={() => handleDeleteService(s.id)} className="p-1.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-md flex items-center justify-center transition-colors shadow-sm" title="Delete Service">
                              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" viewBox="0 0 20 20" fill="currentColor"><path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" /></svg>
                            </button>
                          </div>
                          <div className="absolute top-0 right-0 w-16 h-16 bg-brand-50 rounded-full -mr-8 -mt-8 transition-transform group-hover:scale-150 z-0"></div>
                          <h3 className="font-bold text-lg text-slate-800 relative z-10 pr-12 flex flex-wrap items-center gap-2">
                            <span className="truncate max-w-[150px]">{s.serviceName}</span>
                            {s.dynamicPricingEnabled && <span title="Dynamic Pricing Enabled" className="text-[10px] px-2 py-0.5 bg-brand-100 text-brand-700 font-bold rounded-full whitespace-nowrap">⚡ Auto</span>}
                          </h3>
                          <div className="flex justify-between items-center mt-4 relative z-10">
                            <span className="font-extrabold text-brand-600 text-xl">${s.basePrice}</span>
                            <span className="text-sm font-semibold text-slate-500 bg-slate-100 px-3 py-1 rounded-full shadow-sm">{s.durationMinutes} min</span>
                          </div>
                        </>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {tab === 'slots' && (
          <div className="animate-fade-in space-y-10">
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* Single Slot Manual Creation */}
              <div className="p-6 bg-slate-50 border border-slate-200 rounded-2xl">
                <h3 className="text-lg font-bold text-slate-800 mb-4 flex items-center">
                  <span className="w-8 h-8 rounded bg-white text-slate-600 flex items-center justify-center mr-3 font-mono shadow-sm">1</span>
                  Create Manual Slot
                </h3>
                <form onSubmit={handleCreateSlot} className="space-y-4">
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase">Service</label>
                    <select value={formSlot.serviceId} onChange={(e) => setFormSlot((s) => ({ ...s, serviceId: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none">
                      <option value="">Select Service</option>
                      {services.map((s) => <option key={s.id} value={s.id}>{s.serviceName}</option>)}
                    </select>
                  </div>
                  <div className="space-y-1.5">
                    <label className="text-xs font-bold text-slate-500 uppercase">Date (YYYY-MM-DD)</label>
                    <input type="text" placeholder="2025-05-15" value={formSlot.slotDate} onChange={(e) => setFormSlot((s) => ({ ...s, slotDate: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none" />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1.5">
                      <label className="text-xs font-bold text-slate-500 uppercase">Start Time</label>
                      <input type="time" value={formSlot.startTime} onChange={(e) => setFormSlot((s) => ({ ...s, startTime: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none" />
                    </div>
                    <div className="space-y-1.5">
                      <label className="text-xs font-bold text-slate-500 uppercase">End Time</label>
                      <input type="time" value={formSlot.endTime} onChange={(e) => setFormSlot((s) => ({ ...s, endTime: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none" />
                    </div>
                  </div>
                  <button type="submit" className="w-full py-3 bg-slate-800 text-white rounded-xl font-bold hover:bg-slate-900 transition-colors text-sm">
                    Create Slot
                  </button>
                </form>
              </div>

              {/* Bulk Generation */}
              <div className="p-6 bg-brand-50 border border-brand-200 rounded-2xl relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-brand-100 rounded-full -mr-16 -mt-16 z-0"></div>
                <div className="relative z-10">
                  <h3 className="text-lg font-bold text-slate-800 mb-2 flex items-center">
                    <span className="w-8 h-8 rounded bg-white text-brand-600 flex items-center justify-center mr-3 font-mono shadow-sm">⚡</span>
                    Auto-Generate Slots
                  </h3>
                  <p className="text-sm text-slate-600 mb-5">Fill your entire day instantly using your service duration automatically.</p>
                  
                  <form onSubmit={handleGenerateBulk} className="space-y-4">
                    <div className="space-y-1.5">
                      <label className="text-xs font-bold text-slate-500 uppercase">Service</label>
                      <select value={formBulkSlot.serviceId} onChange={(e) => setFormBulkSlot((s) => ({ ...s, serviceId: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none">
                        <option value="">Select Service</option>
                        {services.map((s) => <option key={s.id} value={s.id}>{s.serviceName}</option>)}
                      </select>
                    </div>
                    <div className="space-y-1.5">
                      <label className="text-xs font-bold text-slate-500 uppercase">Target Date</label>
                      <input type="text" placeholder="YYYY-MM-DD" value={formBulkSlot.slotDate} onChange={(e) => setFormBulkSlot((s) => ({ ...s, slotDate: e.target.value }))} required className="w-full px-4 py-2.5 bg-white rounded-xl border border-slate-200 outline-none" />
                    </div>
                    <button type="submit" className="w-full py-3 bg-brand-600 text-white rounded-xl font-bold hover:bg-brand-700 transition-colors text-sm shadow-md">
                      Generate Day Schedule
                    </button>
                  </form>
                </div>
              </div>
            </div>

            {/* View Slots */}
            <div className="border-t border-slate-100 pt-8">
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6">
                <h3 className="text-xl font-bold text-slate-800">Available Inventory</h3>
                <select onChange={(e) => loadSlotsForService(e.target.value)} className="mt-3 sm:mt-0 px-4 py-2 bg-slate-50 border border-slate-200 rounded-xl font-medium outline-none text-brand-700">
                  <option value="">Filter by Service</option>
                  {services.map((s) => <option key={s.id} value={s.id}>{s.serviceName}</option>)}
                </select>
              </div>

              {slots.length === 0 ? (
                 <div className="text-center py-10 text-slate-500 bg-slate-50 rounded-2xl border border-dashed border-slate-200">
                   Select a service above to view its slots.
                 </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b-2 border-slate-100 text-slate-500 font-bold uppercase text-xs tracking-wider">
                        <th className="py-3 px-4">Date</th>
                        <th className="py-3 px-4">Time Window</th>
                        <th className="py-3 px-4">Price</th>
                        <th className="py-3 px-4">Status</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {slots.map((s) => (
                        <tr key={s.id} className="hover:bg-slate-50 transition-colors">
                          <td className="py-4 px-4 font-medium text-slate-800">{s.slotDate}</td>
                          <td className="py-4 px-4 text-slate-600">{s.startTime.substring(0,5)} &rarr; {s.endTime.substring(0,5)}</td>
                          <td className="py-4 px-4 font-bold text-brand-600">${s.price}</td>
                          <td className="py-4 px-4">
                            <span className={`px-3 py-1 text-xs font-bold rounded-full ${
                              s.status === 'AVAILABLE' ? 'bg-green-100 text-green-700' : 'bg-slate-100 text-slate-500'
                            }`}>
                              {s.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
