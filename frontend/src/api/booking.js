import http from './http'

// GET /user/book?flightId=xxx
export function getFlightDetailForBooking(flightId) {
    return http.get('/api/user/book', { params: { flightId } })
}

// POST /user/book
export function createBooking(dto) {
    return http.post('/api/user/book', dto)
}
