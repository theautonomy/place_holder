@import java.util.List
@import java.util.Map
@import com.example.demo.model.ErrorGroup
@import com.example.demo.model.ErrorLog
@param List<ErrorGroup> groups
@param Map<String, Object> stats
@param double similarityThreshold
@param int minGroupSize
@param int hours

@template.layout.main(title = "Error Groups",
    content = @`
        <div class="fade-in">
            <!-- Header -->
            <div class="mb-8">
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                    <div>
                        <h1 class="text-3xl font-bold text-gray-900">Intelligent Error Grouping</h1>
                        <p class="mt-2 text-gray-600">
                            AI-powered clustering of similar errors using semantic similarity
                        </p>
                    </div>
                </div>
            </div>

            <!-- Statistics Summary -->
            <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-600">Total Groups</p>
                            <p class="mt-2 text-3xl font-bold text-gray-900">${String.valueOf(stats.get("totalGroups"))}</p>
                        </div>
                        <div class="p-3 bg-blue-100 rounded-lg">
                            <svg class="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-600">Errors Clustered</p>
                            <p class="mt-2 text-3xl font-bold text-gray-900">${String.valueOf(stats.get("totalErrorsClustered"))}</p>
                        </div>
                        <div class="p-3 bg-green-100 rounded-lg">
                            <svg class="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-600">Largest Group</p>
                            <p class="mt-2 text-3xl font-bold text-gray-900">${String.valueOf(stats.get("largestGroupSize"))}</p>
                        </div>
                        <div class="p-3 bg-purple-100 rounded-lg">
                            <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
                            </svg>
                        </div>
                    </div>
                </div>

                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                    <div class="flex items-center justify-between">
                        <div>
                            <p class="text-sm font-medium text-gray-600">Avg Group Size</p>
                            <p class="mt-2 text-3xl font-bold text-gray-900">${String.format("%.1f", stats.get("averageGroupSize"))}</p>
                        </div>
                        <div class="p-3 bg-yellow-100 rounded-lg">
                            <svg class="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"></path>
                            </svg>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Filters -->
            <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-8">
                <h2 class="text-lg font-semibold text-gray-900 mb-4">Grouping Parameters</h2>
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                        <label for="similarityThreshold" class="block text-sm font-medium text-gray-700 mb-2">
                            Similarity Threshold: <span class="font-bold text-blue-600">${String.format("%.2f", similarityThreshold)}</span>
                        </label>
                        <input type="range" id="similarityThreshold" min="0.5" max="0.95" step="0.05" value="${similarityThreshold}"
                               class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer">
                        <p class="text-xs text-gray-500 mt-1">Higher = More strict grouping</p>
                    </div>
                    <div>
                        <label for="minGroupSize" class="block text-sm font-medium text-gray-700 mb-2">
                            Min Group Size: <span class="font-bold text-blue-600">${minGroupSize}</span>
                        </label>
                        <input type="range" id="minGroupSize" min="2" max="10" step="1" value="${minGroupSize}"
                               class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer">
                        <p class="text-xs text-gray-500 mt-1">Minimum errors per group</p>
                    </div>
                    <div>
                        <label for="hours" class="block text-sm font-medium text-gray-700 mb-2">
                            Time Window: <span class="font-bold text-blue-600">${hours}h</span>
                        </label>
                        <select id="hours" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500">
                            @if(hours == 1)
                                <option value="1" selected>Last 1 Hour</option>
                            @else
                                <option value="1">Last 1 Hour</option>
                            @endif
                            @if(hours == 6)
                                <option value="6" selected>Last 6 Hours</option>
                            @else
                                <option value="6">Last 6 Hours</option>
                            @endif
                            @if(hours == 24)
                                <option value="24" selected>Last 24 Hours</option>
                            @else
                                <option value="24">Last 24 Hours</option>
                            @endif
                            @if(hours == 168)
                                <option value="168" selected>Last 7 Days</option>
                            @else
                                <option value="168">Last 7 Days</option>
                            @endif
                            @if(hours == 720)
                                <option value="720" selected>Last 30 Days</option>
                            @else
                                <option value="720">Last 30 Days</option>
                            @endif
                        </select>
                    </div>
                </div>
                <div class="mt-4 flex justify-end">
                    <button onclick="applyFilters()" class="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
                        Apply Filters
                    </button>
                </div>
            </div>

            <!-- Error Groups -->
            @if(groups.isEmpty())
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-12 text-center">
                    <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"></path>
                    </svg>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No error groups found</h3>
                    <p class="mt-1 text-sm text-gray-500">Try adjusting the similarity threshold or time window.</p>
                </div>
            @else
                <div class="space-y-6">
                    @for(ErrorGroup group : groups)
                        <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
                            <!-- Group Header -->
                            <div class="px-6 py-4 bg-gray-50 border-b border-gray-200">
                                <div class="flex justify-between items-center">
                                    <div class="flex items-center gap-4">
                                        <span class="px-3 py-1 bg-blue-100 text-blue-800 text-sm font-semibold rounded-full">
                                            ${group.getGroupId()}
                                        </span>
                                        <h3 class="text-lg font-semibold text-gray-900">${group.getRepresentativeErrorType()}</h3>
                                        <span class="px-3 py-1 bg-gray-100 text-gray-700 text-sm font-medium rounded">
                                            ${group.getErrorCount()} errors
                                        </span>
                                        @if(group.getSeverity() != null)
                                            @if(group.getSeverity().equals("CRITICAL"))
                                                <span class="px-3 py-1 bg-red-100 text-red-800 text-sm font-medium rounded">
                                                    ${group.getSeverity()}
                                                </span>
                                            @elseif(group.getSeverity().equals("ERROR"))
                                                <span class="px-3 py-1 bg-orange-100 text-orange-800 text-sm font-medium rounded">
                                                    ${group.getSeverity()}
                                                </span>
                                            @else
                                                <span class="px-3 py-1 bg-yellow-100 text-yellow-800 text-sm font-medium rounded">
                                                    ${group.getSeverity()}
                                                </span>
                                            @endif
                                        @endif
                                    </div>
                                    <button onclick="toggleGroup('${group.getGroupId()}')" class="text-blue-600 hover:text-blue-800 font-medium">
                                        <span id="toggle-${group.getGroupId()}">Show Details</span>
                                    </button>
                                </div>
                                <p class="mt-2 text-gray-600">${group.getRepresentativeErrorMessage()}</p>
                            </div>

                            <!-- Group Details (Initially Hidden) -->
                            <div id="details-${group.getGroupId()}" class="hidden">
                                <div class="px-6 py-4">
                                    <h4 class="text-sm font-semibold text-gray-700 mb-3">Errors in this group:</h4>
                                    <div class="space-y-2">
                                        @for(ErrorLog error : group.getErrors())
                                            <div class="flex items-center justify-between p-3 bg-gray-50 rounded border border-gray-200 hover:bg-gray-100 transition-colors">
                                                <div class="flex-1">
                                                    <a href="/errors/${error.getId()}" class="text-blue-600 hover:text-blue-800 font-medium">
                                                        Error #${error.getId()}
                                                    </a>
                                                    <span class="mx-2 text-gray-400">•</span>
                                                    <span class="text-sm text-gray-600">${error.getApplicationName()}</span>
                                                    <span class="mx-2 text-gray-400">•</span>
                                                    <span class="text-sm text-gray-500">${error.getOccurredAt().toString()}</span>
                                                </div>
                                                <span class="px-2 py-1 text-xs font-medium rounded
                                                    @if(error.getSeverity().equals("CRITICAL"))
                                                        bg-red-100 text-red-800
                                                    @elseif(error.getSeverity().equals("ERROR"))
                                                        bg-orange-100 text-orange-800
                                                    @else
                                                        bg-yellow-100 text-yellow-800
                                                    @endif
                                                ">
                                                    ${error.getSeverity()}
                                                </span>
                                            </div>
                                        @endfor
                                    </div>
                                </div>
                            </div>
                        </div>
                    @endfor
                </div>
            @endif
        </div>

        <script>
            function applyFilters() {
                const threshold = document.getElementById('similarityThreshold').value;
                const minSize = document.getElementById('minGroupSize').value;
                const hours = document.getElementById('hours').value;
                window.location.href = '/groups?similarityThreshold=' + threshold + '&minGroupSize=' + minSize + '&hours=' + hours;
            }

            function toggleGroup(groupId) {
                const details = document.getElementById('details-' + groupId);
                const toggle = document.getElementById('toggle-' + groupId);
                if (details.classList.contains('hidden')) {
                    details.classList.remove('hidden');
                    toggle.textContent = 'Hide Details';
                } else {
                    details.classList.add('hidden');
                    toggle.textContent = 'Show Details';
                }
            }

            // Update slider value display
            document.getElementById('similarityThreshold').addEventListener('input', function(e) {
                const label = e.target.previousElementSibling;
                label.innerHTML = 'Similarity Threshold: <span class="font-bold text-blue-600">' + parseFloat(e.target.value).toFixed(2) + '</span>';
            });

            document.getElementById('minGroupSize').addEventListener('input', function(e) {
                const label = e.target.previousElementSibling;
                label.innerHTML = 'Min Group Size: <span class="font-bold text-blue-600">' + e.target.value + '</span>';
            });

            document.getElementById('hours').addEventListener('change', function(e) {
                const label = e.target.previousElementSibling;
                label.innerHTML = 'Time Window: <span class="font-bold text-blue-600">' + e.target.value + 'h</span>';
            });
        </script>
    `
)
