/**
 * EcomAIGen - Ant Design Theme Configuration
 * Professional Blue Theme customization
 */
import type { ThemeConfig } from 'ant-design-vue';

export const antdTheme: ThemeConfig = {
  token: {
    // Primary Colors
    colorPrimary: '#0369A1',
    colorSuccess: '#10B981',
    colorWarning: '#F59E0B',
    colorError: '#EF4444',
    colorInfo: '#06B6D4',

    // Base colors
    colorBgBase: '#FFFFFF',
    colorBgContainer: '#FFFFFF',
    colorBgElevated: '#FFFFFF',
    colorBgLayout: '#F8FAFC',

    // Text colors
    colorTextBase: '#020617',
    colorTextSecondary: '#475569',
    colorTextTertiary: '#64748B',
    colorTextQuaternary: '#94A3B8',

    // Border colors
    colorBorder: '#E2E8F0',
    colorBorderSecondary: '#CBD5E1',

    // Typography
    fontFamily: "'Open Sans', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
    fontSize: 14,
    fontSizeHeading1: 36,
    fontSizeHeading2: 30,
    fontSizeHeading3: 24,
    fontSizeHeading4: 20,
    fontSizeHeading5: 18,

    // Border Radius
    borderRadius: 8,
    borderRadiusLG: 12,
    borderRadiusSM: 6,
    borderRadiusXS: 4,

    // Spacing
    marginXS: 8,
    marginSM: 12,
    margin: 16,
    marginMD: 20,
    marginLG: 24,
    marginXL: 32,

    // Shadows
    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
    boxShadowSecondary: '0 1px 3px rgba(0, 0, 0, 0.1)',

    // Wireframe
    wireframe: false,
  },

  components: {
    // Button styling
    Button: {
      colorPrimary: '#0369A1',
      primaryShadow: '0 4px 12px rgba(3, 105, 161, 0.3)',
      defaultShadow: '0 2px 6px rgba(0, 0, 0, 0.1)',
      paddingContentHorizontal: 20,
      paddingContentVertical: 10,
      fontWeight: 600,
      borderRadius: 8,
    },

    // Input styling
    Input: {
      colorBgContainer: '#FFFFFF',
      colorBorder: '#E2E8F0',
      activeBorderColor: '#0369A1',
      hoverBorderColor: '#06B6D4',
      borderRadius: 8,
      paddingBlock: 10,
      paddingInline: 16,
    },

    // Card styling
    Card: {
      colorBgContainer: '#FFFFFF',
      colorBorderSecondary: '#E2E8F0',
      borderRadiusLG: 12,
      paddingLG: 24,
      boxShadowTertiary: '0 4px 12px rgba(0, 0, 0, 0.08)',
    },

    // Modal styling
    Modal: {
      contentBg: '#FFFFFF',
      headerBg: '#F8FAFC',
      borderRadiusLG: 16,
      paddingLG: 24,
    },

    // Table styling
    Table: {
      colorBgContainer: '#FFFFFF',
      headerBg: '#F8FAFC',
      headerColor: '#020617',
      borderColor: '#E2E8F0',
      borderRadius: 8,
    },

    // Select styling
    Select: {
      colorBgContainer: '#FFFFFF',
      colorBorder: '#E2E8F0',
      optionSelectedBg: 'rgba(3, 105, 161, 0.1)',
      borderRadius: 8,
    },

    // Form styling
    Form: {
      itemMarginBottom: 20,
      verticalLabelPadding: '0 0 8px',
      labelRequiredMarkColor: '#EF4444',
    },

    // Tag styling
    Tag: {
      borderRadiusSM: 6,
    },

    // Space styling
    Space: {
      verticalSmall: 8,
      verticalMiddle: 16,
      verticalLarge: 24,
    },

    // Divider styling
    Divider: {
      colorText: 'rgba(2, 6, 23, 0.45)',
      marginLG: 24,
      marginXL: 32,
    },

    // Typography styling
    Typography: {
      colorHeading: '#020617',
      colorText: '#475569',
      colorTextSecondary: '#64748B',
    },

    // Menu styling
    Menu: {
      colorBgContainer: '#FFFFFF',
      colorItemBg: 'rgba(3, 105, 161, 0.08)',
      colorItemTextSelected: '#0369A1',
      itemBorderRadius: 8,
    },

    // Dropdown styling
    Dropdown: {
      borderRadiusLG: 12,
      boxShadowSecondary: '0 4px 12px rgba(0, 0, 0, 0.15)',
    },

    // Message styling
    Message: {
      colorBgSpotlight: '#F8FAFC',
      colorNotice: '#0369A1',
      borderRadiusLG: 8,
    },

    // Notification styling
    Notification: {
      colorBgElevated: '#FFFFFF',
      borderRadiusLG: 12,
    },

    // Tooltip styling
    Tooltip: {
      borderRadiusLG: 8,
      colorBgSpotlight: '#0F172A',
    },

    // Popover styling
    Popover: {
      borderRadiusLG: 12,
      boxShadowSecondary: '0 4px 12px rgba(0, 0, 0, 0.15)',
    },

    // Pagination styling
    Pagination: {
      borderRadius: 8,
      itemBg: '#FFFFFF',
      itemSize: 36,
    },

    // Tabs styling
    Tabs: {
      itemActiveColor: '#0369A1',
      itemSelectedColor: '#0369A1',
      inkBarColor: '#0369A1',
    },

    // Badge styling
    Badge: {
      colorError: '#EF4444',
      colorSuccess: '#10B981',
    },

    // Alert styling
    Alert: {
      borderRadiusLG: 12,
      paddingLG: 16,
    },

    // Skeleton styling
    Skeleton: {
      colorBg: '#F1F5F9',
    },
  },

  algorithm: [], // No dark mode algorithm for now, we can add it later
};

// Dark mode theme configuration (optional)
export const antdDarkTheme: ThemeConfig = {
  ...antdTheme,
  token: {
    ...antdTheme.token,
    colorBgBase: '#0F172A',
    colorBgContainer: '#1E293B',
    colorBgElevated: '#334155',
    colorBgLayout: '#0F172A',

    colorTextBase: '#F8FAFC',
    colorTextSecondary: '#CBD5E1',
    colorTextTertiary: '#94A3B8',
    colorTextQuaternary: '#64748B',

    colorBorder: '#334155',
    colorBorderSecondary: '#475569',
  },
  algorithm: [], // Dark mode algorithm can be added here
};
