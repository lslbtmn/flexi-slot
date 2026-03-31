import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { paymentsApi } from '../api/client'

export default function PaymentCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState('Verifying payment...')

  useEffect(() => {
    const reference = searchParams.get('reference')
    if (!reference) {
      setStatus('Invalid payment reference.')
      setTimeout(() => navigate('/customer'), 3000)
      return
    }

    paymentsApi.verify(reference)
      .then((res) => {
        if (res.data.status === 'SUCCESS') {
          setStatus('Payment successful! Redirecting to dashboard...')
        } else {
          setStatus('Payment failed or pending. Redirecting...')
        }
      })
      .catch(() => {
        setStatus('Error verifying payment. Redirecting...')
      })
      .finally(() => {
        setTimeout(() => navigate('/customer'), 3000)
      })
  }, [searchParams, navigate])

  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h2>Payment Status</h2>
      <p>{status}</p>
    </div>
  )
}
