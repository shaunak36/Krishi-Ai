# School Management System (Frontend)

React frontend for an AI-powered school management system, built during my AI Engineering internship at Branding Catalyst Pvt. Ltd. It talks to a Node.js + Express backend that uses Groq (LLaMA) for the AI features.

## Features

- AI chatbot for school-related questions
- Automated grading
- Attendance analysis
- Student performance prediction

> Edit this list to match exactly what the app does. Add screens for login, dashboards, etc. if they exist.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React |
| Backend | Node.js, Express |
| AI | Groq API (LLaMA) |
| Hosting | Netlify (frontend), Render (backend) |

## Getting Started

### Prerequisites

- Node.js 18 or later
- npm
- The backend running locally or deployed (it provides the API and the AI features)

### Installation

```bash
git clone https://github.com/shaunak36/shaunak36-school-management-frontend.git
cd shaunak36-school-management-frontend
npm install
```

### Configuration

Create a `.env` file in the project root and set the backend URL. Use the variable name your code actually reads (for example `REACT_APP_API_URL` for Create React App or `VITE_API_URL` for Vite):

```
REACT_APP_API_URL=http://localhost:5000
```

Do not put any API keys in the frontend. The Groq key lives only on the backend, and `.env` files are listed in `.gitignore`.

### Run

```bash
npm start
```

Use `npm run dev` instead if the project uses Vite.

### Build

```bash
npm run build
```

## Project Structure

```
src/
  components/   Reusable UI components
  pages/        Application screens
  services/     API calls to the backend
```

> Adjust this to match your real folder layout.

## Deployment

- Frontend: Netlify
- Backend: Render

## Author

Shaunak Paul
GitHub: [shaunak36](https://github.com/shaunak36)
LinkedIn: [shaunak-paul-000907321](https://linkedin.com/in/shaunak-paul-000907321)
