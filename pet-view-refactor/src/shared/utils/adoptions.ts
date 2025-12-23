export const isAdoptionFailed = (status?: string | null) => {
  const upper = (status ?? '').toString().toUpperCase()
  return upper === 'REJECTED' || upper === 'FAILED'
}

export const filterAdoptions = <T extends { adoptionStatus?: string; status?: string }>(records: T[] = []) =>
  records.filter(item => !isAdoptionFailed(item.adoptionStatus ?? item.status))
