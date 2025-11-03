// 接口定义
export interface Foster {
  id: number
  pet: {
    pid: number
    name: string
    breed: string
    age: number
    gender: string
    image: string | '/dog.jpg'
  }
  shelter: {
    sid: number
    name: string
    location: string
  }
  startDate: string
  endDate: string | null
  status: 'ongoing' | 'ended'
}

// export interface FosterResponse {
//   code: number
//   data: FosterData
//   message: string
// }

export interface FosterData {
  records: Array<{
    fid: number
    startDate: string
    endDate: string | null
    pid: number
    name: string
    breed: string
    age: number
    gender: string
    image: string | null
    sid: number
    sname: string
    location: string
  }>
    total: number, 
    size: number,
    current: number, 
    pages: number 
}
       

