import { Outlet, Link, useNavigate } from 'react-router-dom'

export default function Layout() {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  const navigate = useNavigate()

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('role')
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col font-sans">
      <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-200 shadow-sm transition-all duration-300">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            <div className="flex items-center space-x-8">
              <Link to="/" className="text-2xl font-bold bg-gradient-to-r from-brand-600 to-indigo-600 bg-clip-text text-transparent transform hover:scale-105 transition-transform duration-300">
                FlexiSlot
              </Link>
              <div className="hidden md:flex space-x-4">
                <Link to="/browse" className="text-slate-600 hover:text-brand-600 font-medium transition-colors">
                  Browse Services
                </Link>
                {token && role === 'BUSINESS_OWNER' && (
                  <Link to="/business" className="text-slate-600 hover:text-brand-600 font-medium transition-colors">
                    My Business
                  </Link>
                )}
                {token && role === 'CUSTOMER' && (
                  <Link to="/customer" className="text-slate-600 hover:text-brand-600 font-medium transition-colors">
                    My Bookings
                  </Link>
                )}
              </div>
            </div>

            <div className="flex items-center space-x-4">
              {token ? (
                <button 
                  onClick={handleLogout} 
                  className="px-4 py-2 rounded-full text-sm font-semibold text-slate-700 bg-slate-100 hover:bg-slate-200 transition-colors shadow-sm"
                >
                  Logout
                </button>
              ) : (
                <>
                  <Link to="/login" className="text-slate-600 hover:text-brand-600 font-medium transition-colors">
                    Login
                  </Link>
                  <Link to="/register" className="px-5 py-2 rounded-full text-sm font-semibold text-white bg-brand-600 hover:bg-brand-700 shadow-md hover:shadow-lg transition-all transform hover:-translate-y-0.5">
                    Register
                  </Link>
                </>
              )}
            </div>
          </div>
        </div>
      </nav>

      <main className="flex-grow w-full max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
        <Outlet />
      </main>
    </div>
  )
}
