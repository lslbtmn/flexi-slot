import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

export default function Home() {
    const [businesses, setBusinesses] = useState([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const fetchBusinesses = async () => {
            try {
                const { data } = await api.get('/business', { params: { page: 0, size: 50 } })
                setBusinesses(data.content || [])
            } catch (err) {
                setError('Failed to load businesses')
            } finally {
                setLoading(false)
            }
        }
        fetchBusinesses()
    }, [])

    return (
        <div className="flex flex-col space-y-16">
            {/* Hero Section */}
            <section className="relative py-20 lg:py-32 px-4 text-center space-y-8 rounded-3xl overflow-hidden glass shadow-2xl bg-gradient-to-br from-brand-50 to-indigo-50 border border-white">
                <div className="absolute top-0 right-0 -mr-20 -mt-20 w-72 h-72 bg-brand-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
                <div className="absolute bottom-0 left-0 -ml-20 -mb-20 w-72 h-72 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
                
                <h1 className="text-5xl lg:text-7xl font-extrabold text-slate-900 tracking-tight z-10 relative">
                    Book what you love, <br/>
                    <span className="bg-gradient-to-r from-brand-600 to-indigo-600 bg-clip-text text-transparent">when you want.</span>
                </h1>
                <p className="max-w-2xl mx-auto text-xl text-slate-600 z-10 relative">
                    FlexiSlot brings the best local services right to your fingertips. Browse, book, and easily manage all your appointments in one place.
                </p>
                <div className="pt-4 z-10 relative">
                    <Link to="/browse" className="inline-block px-8 py-4 rounded-full text-lg font-bold text-white bg-brand-600 hover:bg-brand-700 shadow-xl hover:shadow-2xl transition-all transform hover:-translate-y-1">
                        Explore Services
                    </Link>
                </div>
            </section>

            {/* Businesses Grid */}
            <section className="space-y-8">
                <div className="flex items-center justify-between">
                    <h2 className="text-3xl font-bold text-slate-800">Featured Businesses</h2>
                </div>

                {error && (
                    <div className="p-4 rounded-xl bg-red-50 text-red-600 border border-red-100 flex items-center">
                        <p className="font-medium">{error}</p>
                    </div>
                )}

                {loading ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                        {[1, 2, 3].map((i) => (
                            <div key={i} className="h-48 rounded-2xl bg-slate-200 animate-pulse"></div>
                        ))}
                    </div>
                ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                        {businesses.map((b) => (
                            <div key={b.id} className="group relative bg-white rounded-2xl p-6 shadow-sm hover:shadow-xl border border-slate-100 transition-all duration-300 transform hover:-translate-y-1 flex flex-col h-full">
                                <div className="flex-grow space-y-4">
                                    <div className="w-12 h-12 rounded-full bg-brand-100 flex items-center justify-center text-brand-600 text-xl font-bold mb-4">
                                        {b.name.charAt(0).toUpperCase()}
                                    </div>
                                    <h3 className="text-xl font-bold text-slate-900 group-hover:text-brand-600 transition-colors">{b.name}</h3>
                                    <div className="space-y-2 text-sm text-slate-600">
                                        <div className="flex items-center">
                                            <span className="font-medium text-slate-700 w-20">Type:</span> {b.serviceType}
                                        </div>
                                        <div className="flex items-center">
                                            <span className="font-medium text-slate-700 w-20">Location:</span> {b.location}
                                        </div>
                                    </div>
                                </div>
                                <div className="mt-6 pt-4 border-t border-slate-100">
                                    <Link to={`/browse?businessId=${b.id}`} className="block w-full text-center px-4 py-2 rounded-xl text-brand-600 font-semibold bg-brand-50 hover:bg-brand-100 transition-colors">
                                        View Offerings
                                    </Link>
                                </div>
                            </div>
                        ))}
                        {businesses.length === 0 && !error && (
                            <div className="col-span-full py-12 text-center text-slate-500 bg-slate-50 rounded-2xl border border-dashed border-slate-300">
                                No businesses are currently registered. Check back later!
                            </div>
                        )}
                    </div>
                )}
            </section>
        </div>
    )
}
