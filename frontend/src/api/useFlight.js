import http from '@/api/http'

// GET /user/flights/cities?keyword=xxx
export function listCities(keyword = '') {
    return http.get('/api/user/flights/cities', { params: { keyword } })
}

// GET /user/flights/search?fromCity=..&toCity=..&date=..&page=..&size=..
export function searchFlights({ fromCity, toCity, date, page = 1, size = 10 }) {
    return http.get('/api/user/flights/search', {
        params: { fromCity, toCity, date, page, size }
    })
}
