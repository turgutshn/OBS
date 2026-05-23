/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef4ff',
          100: '#dbe5ff',
          200: '#bcceff',
          300: '#8eafff',
          400: '#5b85ff',
          500: '#3a63f5',
          600: '#2745e0',
          700: '#1f37b6',
          800: '#1d3293',
          900: '#1d2f76',
        },
      },
      fontFamily: {
        sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
};
