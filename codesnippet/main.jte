@param String title = "Error Analysis"
@param gg.jte.Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/svg+xml" href="/favicon.svg">
    <title>${title} - Error Analysis System</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <style>
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .fade-in { animation: fadeIn 0.3s ease-out; }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
    <!-- Navigation -->
    <nav class="bg-gradient-to-r from-gray-800 to-gray-900 text-white shadow-lg">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div class="flex justify-between items-center h-16">
                <div class="flex items-center">
                    <h1 class="text-2xl font-bold tracking-tight">
                        <span class="text-blue-400">Error</span>Analysis
                    </h1>
                </div>
                <div class="flex space-x-1">
                    <a href="/dashboard" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Dashboard
                    </a>
                    <a href="/errors" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Errors
                    </a>
                    <a href="/groups" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Groups
                    </a>
                    <a href="/search" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Search
                    </a>
                    <a href="/chat" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        AI Chat
                    </a>
                    <a href="/analytics" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Analytics
                    </a>
                    <a href="/reports" class="px-4 py-2 rounded-lg hover:bg-gray-700 transition-colors duration-200">
                        Reports
                    </a>
                </div>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        ${content}
    </main>

    <!-- Footer -->
    <footer class="bg-white border-t border-gray-200 mt-12">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <p class="text-center text-gray-600 text-sm">
                Error Analysis System - Powered by AI &amp; Vector Search
            </p>
        </div>
    </footer>
</body>
</html>
