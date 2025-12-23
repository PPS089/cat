import { House, Grid, Plus, Star, OfficeBuilding, Warning, Calendar, User, Setting } from '@element-plus/icons-vue'

interface NavItem {
  labelKey: string
  path: string
  icon: any
  badgeKey?: 'petCount' | 'adoptionCount' | 'fosterCount' | 'healthAlertCount' | 'eventsCount'
}

interface NavSection {
  titleKey: string
  items: NavItem[]
}

export const navigationSections: NavSection[] = [
  {
    titleKey: 'nav.main',
    items: [
      { labelKey: 'nav.home', path: '/user', icon: House },
    ],
  },
  {
    titleKey: 'nav.petManagement',
    items: [
      { labelKey: 'nav.myPets', path: '/user/pets', icon: Grid, badgeKey: 'petCount' },
      { labelKey: 'nav.addPet', path: '/user/adoption-pets', icon: Plus },
      { labelKey: 'nav.adoptionRecords', path: '/user/adoptions', icon: Star, badgeKey: 'adoptionCount' },
      { labelKey: 'nav.fosteringRecords', path: '/user/fosters', icon: OfficeBuilding, badgeKey: 'fosterCount' },
      { labelKey: 'nav.eventRecords', path: '/user/events', icon: Calendar, badgeKey: 'eventsCount' },
    ],
  },
  {
    titleKey: 'nav.healthCenter',
    items: [
      { labelKey: 'nav.healthAlerts', path: '/user/health-alerts', icon: Warning, badgeKey: 'healthAlertCount' },
    ],
  },
  {
    titleKey: 'nav.userCenter',
    items: [
      { labelKey: 'nav.profile', path: '/user/profile', icon: User },
      { labelKey: 'nav.settings', path: '/user/settings', icon: Setting },
    ],
  },
]

export const adminNavigationSections: NavSection[] = [
  {
    titleKey: 'nav.management',
    items: [
      { labelKey: 'nav.adminAdoptions', path: '/admin/adoptions', icon: Star },
      { labelKey: 'nav.adminFosters', path: '/admin/fosters', icon: OfficeBuilding },
      { labelKey: 'nav.adminPets', path: '/admin/pets', icon: Grid },
      { labelKey: 'nav.adminSpecies', path: '/admin/species', icon: Setting },
      { labelKey: 'nav.adminArticles', path: '/admin/articles', icon: Plus },
    ],
  },
]
