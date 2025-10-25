<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Eventx — Hackathon Ready</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script>
    tailwind.config = {
      theme: {
        extend: {
          colors: { brand: { DEFAULT: '#a78bfa', dark: '#8b5cf6' } },
          boxShadow: { glow: '0 10px 40px rgba(167,139,250,0.25)' },
        }
      }
    }
  </script>
  <script src="https://unpkg.com/lucide@latest"></script>
  <script src="https://cdn.jsdelivr.net/npm/xlsx@0.18.5/dist/xlsx.full.min.js"></script>
  
    <link rel="preload" href="https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js" as="script">
  <link rel="preload" href="https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js" as="script">
  <link rel="preload" href="https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js" as="script">
  
  <style>
    html,body {height:100%}
    body {font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Arial; color: #e2e8f0;}
    .link {color:#a78bfa}
    /* Dark theme card style */
    .card { @apply bg-slate-900/80 backdrop-blur-xl shadow-2xl rounded-3xl p-6 border border-slate-700 transition-all duration-300 hover:shadow-glow; }
    /* Dark theme button styles */
    .btn { @apply inline-flex items-center justify-center gap-2 rounded-xl px-6 py-3 font-semibold transition-all duration-300 active:scale-[0.98] focus:outline-none focus:ring-4 focus:ring-brand/50; }
    .btn-primary { @apply btn bg-brand text-white hover:bg-brand-dark shadow-xl; }
    .btn-ghost { @apply btn bg-transparent hover:bg-white/10 text-slate-200; }
    .input { @apply w-full rounded-xl border border-slate-700 bg-slate-800 text-white px-4 py-3 outline-none transition focus:ring-2 focus:ring-brand; }
    /* New badge style for dark theme */
    .badge { @apply inline-flex items-center gap-1 rounded-full px-3 py-1 text-xs font-semibold; background-color: #334155; color: #cbd5e1; border: 1px solid #475569; }
    /* Animation and effect updates */
    .fade-in { animation: fade .5s ease-out both }
    @keyframes fade { from {opacity:0; transform:translateY(12px)} to {opacity:1; transform:none} }
    .glow-text { text-shadow: 0 0 10px rgba(167,139,250,0.5), 0 0 20px rgba(167,139,250,0.3); }
    .pulse-effect { animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; }
    @keyframes pulse { 0%, 100% {opacity: 1;} 50% {opacity: .5;} }
    .card:hover { transform: translateY(-4px); }
  </style>
</head>
<body class="min-h-full bg-slate-950 text-slate-200">
    <div id="loading" class="min-h-screen flex items-center justify-center">
    <div class="text-center max-w-4xl mx-auto mb-12">
      <h1 class="text-4xl sm:text-5xl font-extrabold tracking-tight glow-text">Loading <span class="text-brand">EventX</span></h1>
      <p class="text-slate-400 mt-4 text-lg">Initializing Firebase connection...</p>
      <div class="mt-6">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-brand"></div>
      </div>
      <p class="text-slate-500 mt-4 text-sm">Trying to load Firebase for a secure connection.</p>
    </div>
  </div>

    <div id="app" class="min-h-screen flex-col hidden">
    <header class="sticky top-0 z-40 bg-slate-900/80 backdrop-blur border-b border-slate-700">
      <div class="max-w-7xl mx-auto px-6 h-20 grid grid-cols-3 items-center">
        <nav class="flex items-center gap-4">
          <a href="#/" class="btn btn-ghost hidden sm:inline-flex">Home</a>
          <a href="#/dashboard" class="btn btn-ghost hidden sm:inline-flex">Dashboard</a>
        </nav>
        <div class="flex items-center justify-end gap-3" id="authArea"></div>
      </div>
    </header>

    <main class="flex-1">
      <section id="view" class="max-w-7xl mx-auto px-6 py-12"></section>
    </main>

    <footer class="border-t mt-16 border-slate-700">
      <div class="max-w-7xl mx-auto px-6 py-8 text-sm text-slate-400 flex flex-wrap items-center justify-between gap-4">
        <p>© <span id="year"></span> Eventx. Hackathon-ready single-file app.</p>
        <div class="flex items-center gap-3">
          <a class="underline" href="#/about">About</a>
          <a class="underline" href="#/terms">Terms</a>
        </div>
      </div>
    </footer>
  </div>

  <div id="toast" class="fixed bottom-8 left-1/2 -translate-x-1/2 hidden"></div>

    <script type="module">
    // Firebase configuration and loading logic
    const firebaseConfig = {
      apiKey: "AIzaSyCoe2Cu-djvFvq1hX7Y9cVepkNKTKaFJcg",
      authDomain: "eventx-dbbcf.firebaseapp.com",
      projectId: "eventx-dbbcf",
      storageBucket: "eventx-dbbcf.firebasestorage.app",
      messagingSenderId: "776388177883",
      appId: "1:776388177883:web:4dee2081ab6bf9ed8801fe"
    };
    
    // Main Application Logic (all functions and variables defined here)
    async function main() {
      // Hide loading state and show the main app container
      document.getElementById('loading').style.display = 'none';
      document.getElementById('app').style.display = 'flex';
    
      // --------------------
      // Tiny SPA Framework
      // --------------------
      const $ = s => document.querySelector(s);
      const $$ = s => Array.from(document.querySelectorAll(s));
      const routes = {};
      const view = $('#view');
      const authArea = $('#authArea');

      // Expose key functions to the global scope via a single 'app' object
      window.app = {
        navigate: (path) => { location.hash = path; },
        signOut: async () => {
          try {
            await window.firebase.signOut(window.firebase.auth);
            session = null; 
            renderHeader(); 
            window.app.navigate('#/');
          } catch (error) {
            console.error('Sign out error:', error);
          }
        },
        onLogin: async () => {
          try {
            const email = $('#li_email').value.trim();
            const password = $('#li_password').value;
            await signIn({ email, password });
            setToast('Welcome back!');
            renderHeader();
            window.app.navigate('#/dashboard');
          } catch (err) { setToast(err.message, false); }
        },
        onRegister: async (role) => {
          try {
            const name = $('#su_name').value.trim();
            const email = $('#su_email').value.trim();
            const password = $('#su_password').value;
            await signUp({ email, password, role, name });
            setToast('Account created!');
            renderHeader();
            if (role === 'organiser') window.app.navigate('#/dashboard'); else window.app.navigate('#/');
          } catch (err) { setToast(err.message, false); }
        },
        doSearch: () => {
          const q = $('#search').value.trim();
          window.app.navigate(#/?q=${encodeURIComponent(q)});
          render();
        },
        goRegister: (eventId) => {
          if(!session){ window.app.navigate('#/login'); setToast('Please login/register first', false); return }
          if(session.role!=='student'){ setToast('Login as student to register', false); return }
          window.app.navigate(#/register/${eventId})
        },
        updateTotal: (price) => {
          const qty = Math.max(1, parseInt($('#st_qty').value || '1'));
          $('#st_amount').value = (price * qty).toFixed(2);
        },
        goPayment: (eventId) => {
          const name = $('#st_name').value.trim();
          const qty = Math.max(1, parseInt($('#st_qty').value||'1'));
          const ticket = $('#st_ticket').value.trim()||'General';
          const amount = parseFloat($('#st_amount').value||'0');
          const e = appData.events.find(x=>x.id===eventId);
          if(!name){ setToast('Please enter your name', false); return }
          const slotsAvailable = e.slots_available || 0;
          if(qty > slotsAvailable){ 
            setToast(Not enough slots left. Only ${slotsAvailable} slots available., false); 
            return 
          }
          view.innerHTML = `
            <div class="max-w-lg mx-auto card fade-in text-center">
              <h2 class="text-2xl font-bold">Payment Portal</h2>
              <p class="text-slate-400 mt-2">Securely pay for your registration. Total: ₹${amount.toFixed(2)}</p>
              <div class="mt-6 grid grid-cols-3 gap-4">
                <button class="btn btn-ghost border border-slate-700 hover:border-brand hover:text-brand" onclick="window.app.mockPay('${eventId}', '${name}', ${qty}, '${ticket}', ${amount}, 'UPI')">UPI</button>
                <button class="btn btn-ghost border border-slate-700 hover:border-brand hover:text-brand" onclick="window.app.mockPay('${eventId}', '${name}', ${qty}, '${ticket}', ${amount}, 'CARD')">Card</button>
                <button class="btn btn-ghost border border-slate-700 hover:border-brand hover:text-brand" onclick="window.app.mockPay('${eventId}', '${name}', ${qty}, '${ticket}', ${amount}, 'NET')">NetBanking</button>
              </div>
              <button class="btn btn-ghost mt-6" onclick="window.app.navigate('#/register/${eventId}')">Back</button>
          </div>`;
        },
        mockPay: async (eventId, name, qty, ticket, amount, method) => {
          try {
            const e = appData.events.find(x=>x.id===eventId);
            const reg = { id: uid(), user_email: session.email, name, ticket, qty, paid:true, amount, method, created_at: new Date().toISOString() };
            e.participants.push(reg);
            e.updated_at = new Date().toISOString();
            await updateEventRegistration(eventId, true);
            await saveEvent(e);
            view.innerHTML = `
              <div class="max-w-lg mx-auto card text-center fade-in">
                <div class="mx-auto w-16 h-16 rounded-full bg-emerald-700 text-white flex items-center justify-center mb-4"><i data-lucide=check-circle class=w-10 h-10></i></div>
                <h2 class="text-3xl font-extrabold mt-3">Payment Successful</h2>
                <p class="text-slate-400 mt-2">Your registration is confirmed. A receipt has been sent to your email.</p>
                <div class="mt-6 text-left bg-slate-800 border border-slate-700 rounded-xl p-5">
                  <div class="font-semibold text-lg">Ticket Details</div>
                  <div class="text-sm mt-2 space-y-1">Event: ${e.title}<br/>Name: ${name}<br/>Qty: ${qty}<br/>Ticket: ${ticket}<br/>Amount: ₹${amount.toFixed(2)}<br/>Txn: ${reg.id}</div>
              </div>
                <button class="btn btn-primary mt-6" onclick="window.app.navigate('#/')">Go Home</button>
          </div>`;
            setToast('Registered successfully');
            lucide.createIcons();
          } catch (error) {
            console.error('Payment error:', error);
            setToast('Payment failed. Please try again.', false);
          }
        },
        openEventModal: (id) => {
          const isEdit = !!id;
          const e = isEdit ? appData.events.find(x=>x.id===id) : { 
            title:'', description:'', category:'', date_time:new Date().toISOString().slice(0,16), 
            venue:'', max:0, price:0, is_published:false, 
            registration_count: 0, slots_available: 0 
          };
          const dlg = $('#eventModal');
          dlg.innerHTML = `
            <form method="dialog" class="card rounded-2xl fade-in">
              <h3 class="text-2xl font-bold mb-4">${isEdit?'Edit':'Create'} Event</h3>
              <div class="grid sm:grid-cols-2 gap-4">
                ${field('ev_title','Title','text',value="${e.title}")}
                ${field('ev_category','Category','text',value="${e.category||''}")}
                <label class="block text-sm font-medium text-slate-400">Date & Time
                  <input id="ev_date" type="datetime-local" class="input mt-1" value="${(isEdit? new Date(e.date_time) : new Date()).toISOString().slice(0,16)}" />
              </label>
                ${field('ev_venue','Venue','text',value="${e.venue}")}
                ${field('ev_max','Max Participants','number',min=1 value="${e.max||0}" oninput="window.app.updateSlotsAvailable()")}
                ${field('ev_price','Price (₹)','number',min=0 step="10" value="${e.price||0}")}
              </div>
              <div class="grid sm:grid-cols-2 gap-4 mt-4">
                <div class="bg-slate-800/50 p-4 rounded-lg">
                  <label class="block text-sm font-medium text-slate-400 mb-2">Registration Count</label>
                  <div class="text-2xl font-bold text-brand" id="registration_count_display">${e.registration_count || 0}</div>
                  <p class="text-xs text-slate-500 mt-1">Current registrations</p>
              </div>
              <div class="bg-slate-800/50 p-4 rounded-lg">
                <label class="block text-sm font-medium text-slate-400 mb-2">Slots Available</label>
                <div class="text-2xl font-bold text-green-400" id="slots_available_display">${e.slots_available || e.max || 0}</div>
                <p class="text-xs text-slate-500 mt-1">Remaining slots</p>
          </div>
          </div>
            <label class="block text-sm font-medium text-slate-400 mt-4">Description
              <textarea id="ev_desc" class="input mt-1" rows="4">${e.description||''}</textarea>
            </label>
            <div class="mt-6 flex items-center justify-end gap-3">
              <button class="btn btn-ghost">Cancel</button>
              <button class="btn btn-primary" onclick="window.app.saveEvent('${id||''}');return false;">Save</button>
          </div>
          </form>`;
          dlg.showModal();
          lucide.createIcons();
          
          window.app.updateSlotsAvailable = function() {
            const max = parseInt($('#ev_max')?.value || 0);
            const currentRegistrations = e.registration_count || 0;
            const available = Math.max(0, max - currentRegistrations);
            $('#slots_available_display').textContent = available;
          };
        },
        updateSlotsAvailable: () => {
          const max = parseInt($('#ev_max')?.value || 0);
          const e = appData.events.find(x => x.id === $('#ev_id').value);
          const currentRegistrations = e ? e.registration_count : 0;
          const available = Math.max(0, max - currentRegistrations);
          $('#slots_available_display').textContent = available;
        },
        openAnnounceModal: (id) => {
          const e = appData.events.find(x=>x.id===id);
          if(e.organiser_email!==session.email){ setToast('Forbidden', false); return }
          const dlg = document.createElement('dialog');
          dlg.className = 'p-0 rounded-2xl w-full max-w-lg';
          dlg.innerHTML = `
            <form method="dialog" class="card fade-in">
              <h3 class="text-2xl font-bold">Announcement — ${e.title}</h3>
              <p class="text-sm text-slate-400 mt-2">This message will be shown to students on the event details page.</p>
              <textarea id="announcement_text" class="input mt-4" rows="5" placeholder="Enter announcement...">${e.announcement||''}</textarea>
              <div class="mt-6 flex items-center justify-end gap-3">
                <button class="btn btn-ghost">Cancel</button>
                <button class="btn btn-primary" onclick="window.app.saveAnnouncement('${id}');return false;">Publish</button>
              </div>
            </form>`;
          document.body.appendChild(dlg); dlg.showModal();
          dlg.addEventListener('close', ()=> dlg.remove());
          lucide.createIcons();
        },
        saveAnnouncement: async (id) => {
          try {
            const e = appData.events.find(x=>x.id===id);
            if(e.organiser_email!==session.email){ setToast('Forbidden', false); return }
            e.announcement = $('#announcement_text')?.value?.trim() || '';
            e.updated_at = new Date().toISOString();
            await saveEvent(e);
            document.querySelector('dialog').close();
            setToast('Announcement published');
            render();
          } catch (error) {
            console.error('Error saving announcement:', error);
            setToast('Failed to save announcement', false);
          }
        },
        saveEvent: async (id) => {
          try {
            const title = $('#ev_title')?.value?.trim() || '';
            const category = $('#ev_category')?.value?.trim() || '';
            const date_time = new Date($('#ev_date')?.value || new Date()).toISOString();
            const venue = $('#ev_venue')?.value?.trim() || '';
            const max = Math.max(1, parseInt($('#ev_max')?.value||'1'));
            const price = Math.max(0, parseFloat($('#ev_price')?.value||'0'));
            const description = $('#ev_desc')?.value?.trim() || '';
            if(!title){ setToast('Title is required', false); return }

            if(id){
              const e = appData.events.find(x=>x.id===id);
              if(e.organiser_email!==session.email){ setToast('Forbidden: not your event', false); return }
              const existingRegistrations = e.registration_count || 0;
              const existingParticipants = e.participants || [];
              Object.assign(e,{
                title,category,date_time,venue,max,price,description,
                registration_count: existingRegistrations,
                participants: existingParticipants,
                slots_available: Math.max(0, max - existingRegistrations),
                updated_at:new Date().toISOString()
              });
              await saveEvent(e);
            } else {
              const newEvent = { 
                title, category, date_time, venue, max, price, description, 
                organiser_email: session.email, is_published:false, announcement: '', 
                participants:[], 
                registration_count: 0,
                slots_available: max,
                created_at:new Date().toISOString(), updated_at:new Date().toISOString() 
              };
              await saveEvent(newEvent);
            }
            $('#eventModal').close();
            setToast('Saved');
            render();
          } catch (error) {
            console.error('Error saving event:', error);
            setToast('Failed to save event', false);
          }
        },
        togglePublish: async (id) => {
          try {
            const e = appData.events.find(x=>x.id===id);
            if(e.organiser_email!==session.email){ setToast('Forbidden', false); return }
            e.is_published = !e.is_published; 
            e.updated_at = new Date().toISOString(); 
            await saveEvent(e);
            render();
            setToast(e.is_published?'Event published!':'Event unpublished.');
          } catch (error) {
            console.error('Error toggling publish:', error);
            setToast('Failed to update event', false);
          }
        },
        deleteEvent: async (id) => {
          try {
            const e = appData.events.find(x=>x.id===id);
            if(e.organiser_email!==session.email){ setToast('Forbidden', false); return }
            if(!confirm('Are you sure you want to delete this event? This action cannot be undone.')) return;
            await deleteEvent(id);
            render();
            setToast('Event deleted successfully.');
          } catch (error) {
            console.error('Error deleting event:', error);
            setToast('Failed to delete event', false);
          }
        },
        openParticipants: (id) => {
          const e = appData.events.find(x=>x.id===id);
          if(e.organiser_email!==session.email){ setToast('Forbidden', false); return }
          const rows = e.participants.map(p=><tr class="border-t border-slate-700"><td class="py-3 px-2">${p.name}</td><td class="px-2">${p.user_email}</td><td class="px-2">${p.ticket}</td><td class="px-2">${p.qty}</td><td class="px-2">${p.paid?'Paid':'Unpaid'}</td><td class="px-2">₹${p.amount.toFixed(2)}</td><td class="px-2">${new Date(p.created_at).toLocaleString()}</td></tr>).join('') || <tr><td colspan=7 class="py-6 text-center text-slate-500">No participants yet</td></tr>;

          const dlg = document.createElement('dialog');
          dlg.className = 'p-0 rounded-2xl w-full max-w-4xl';
          dlg.innerHTML = `
            <form method="dialog" class="card fade-in">
              <div class="flex items-center justify-between">
                <h3 class="text-2xl font-bold">Participants — ${e.title}</h3>
                <div class="flex gap-2">
                  <button class="btn btn-ghost" onclick="window.app.exportCSV('${id}');return false;"><i data-lucide=download class=w-5 h-5></i>CSV</button>
                  <button class="btn btn-primary" onclick="window.app.exportXLSX('${id}');return false;"><i data-lucide=file-spreadsheet class=w-5 h-5></i>Excel</button>
              </div>
            </div>
              <div class="overflow-auto max-h-[60vh] mt-4">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="text-left text-slate-400 border-b border-slate-700">
                      <th class="py-3 px-2">Name</th><th>Email</th><th>Ticket</th><th>Qty</th><th>Status</th><th>Amount</th><th>Registered At</th></tr>
                  </thead>
                  <tbody>${rows}</tbody>
                </table>
              </div>
              <div class="mt-6 text-right"><button class="btn btn-ghost">Close</button></div>
            </form>`;
          document.body.appendChild(dlg); dlg.showModal();
          dlg.addEventListener('close', ()=> dlg.remove());
          lucide.createIcons();
        },
        exportCSV: (eventId) => {
          const e = appData.events.find(x=>x.id===eventId);
          const rows = e.participants.map(p=>({
            Event: e.title,
            Name: p.name,
            Email: p.user_email,
            Ticket: p.ticket,
            Quantity: p.qty,
            PaymentStatus: p.paid?'Paid':'Unpaid',
            Amount: p.amount,
            RegisteredAt: p.created_at,
            TxnId: p.id
          }));
          const csv = [Object.keys(rows[0]||{Event:'',Name:'',Email:'',Ticket:'',Quantity:0,PaymentStatus:'',Amount:0,RegisteredAt:'',TxnId:''}).join(','), ...rows.map(r=>Object.values(r).map(v=>"${String(v).replaceAll('"','""')}").join(','))].join('\n');
          const blob = new Blob([csv], {type:'text/csv'});
          const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = ${slug(e.title)}-participants.csv; a.click(); URL.revokeObjectURL(a.href);
          setToast('Exporting CSV...');
        },
        exportXLSX: (eventId) => {
          const e = appData.events.find(x=>x.id===eventId);
          const rows = e.participants.map(p=>({
            Event: e.title,
            Name: p.name,
            Email: p.user_email,
            Ticket: p.ticket,
            Quantity: p.qty,
            PaymentStatus: p.paid?'Paid':'Unpaid',
            Amount: p.amount,
            RegisteredAt: new Date(p.created_at).toLocaleString(),
            TxnId: p.id
          }));
          const ws = XLSX.utils.json_to_sheet(rows);
          const wb = XLSX.utils.book_new();
          XLSX.utils.book_append_sheet(wb, ws, 'Participants');
          XLSX.writeFile(wb, ${slug(e.title)}-participants.xlsx);
          setToast('Exporting Excel file...');
        },
      };

      window.addEventListener('hashchange', render);

      function setToast(msg, ok=true){
        const t = $('#toast');
        t.className = fixed bottom-8 left-1/2 -translate-x-1/2 px-5 py-3 rounded-xl text-sm text-white shadow-lg ${ok?'bg-emerald-600':'bg-rose-600'} fade-in;
        t.textContent = msg; t.style.display='block';
        setTimeout(()=> t.style.display='none', 2500);
      }

      // --------------------
      // Firebase Storage & Models
      // --------------------
      let appData = { events: [] };
      let users = [];
      let session = null;

      function uid(){ return crypto.randomUUID ? crypto.randomUUID() : (Date.now()+""+Math.random()).replace('.','') }
      
      async function loadEvents() {
        try {
          const eventsRef = window.firebase.collection(window.firebase.db, 'events');
          const snapshot = await window.firebase.getDocs(eventsRef);
          appData.events = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        } catch (error) {
          console.error('Error loading events:', error);
          throw error; // Re-throw to be caught by main()
        }
      }

      async function loadUsers() {
        try {
          const usersRef = window.firebase.collection(window.firebase.db, 'users');
          const snapshot = await window.firebase.getDocs(usersRef);
          users = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        } catch (error) {
          console.error('Error loading users:', error);
          throw error; // Re-throw to be caught by main()
        }
      }

      async function saveEvent(eventData) {
        try {
          if (!eventData.registration_count) eventData.registration_count = 0;
          if (!eventData.slots_available) eventData.slots_available = eventData.max || 0;
          if (!eventData.participants) eventData.participants = [];
          eventData.slots_available = Math.max(0, (eventData.max || 0) - (eventData.registration_count || 0));
          if (eventData.id) {
            const eventRef = window.firebase.doc(window.firebase.db, 'events', eventData.id);
            await window.firebase.updateDoc(eventRef, eventData);
          } else {
            const eventsRef = window.firebase.collection(window.firebase.db, 'events');
            const docRef = await window.firebase.addDoc(eventsRef, eventData);
            eventData.id = docRef.id;
          }
        } catch (error) {
          console.error('Error saving event:', error);
          throw new Error('Failed to save event to Firebase: ' + error.message);
        }
      }

      async function deleteEvent(eventId) {
        try {
          const eventRef = window.firebase.doc(window.firebase.db, 'events', eventId);
          await window.firebase.deleteDoc(eventRef);
          await loadEvents();
        } catch (error) {
          console.error('Error deleting event:', error);
          throw error;
        }
      }

      async function saveUser(userData) {
        try {
          const usersRef = window.firebase.collection(window.firebase.db, 'users');
          await window.firebase.addDoc(usersRef, userData);
          await loadUsers();
        } catch (error) {
          console.error('Error saving user:', error);
          throw error;
        }
      }

      async function updateEventRegistration(eventId, increment = true) {
        try {
          const eventRef = window.firebase.doc(window.firebase.db, 'events', eventId);
          const eventDoc = await window.firebase.getDoc(eventRef);
          
          if (eventDoc.exists()) {
            const eventData = eventDoc.data();
            const newRegistrationCount = Math.max(0, (eventData.registration_count || 0) + (increment ? 1 : -1));
            const newSlotsAvailable = Math.max(0, (eventData.max || 0) - newRegistrationCount);
            
            await window.firebase.updateDoc(eventRef, {
              registration_count: newRegistrationCount,
              slots_available: newSlotsAvailable,
              updated_at: new Date().toISOString()
            });
            return true;
          }
          return false;
        } catch (error) {
          console.error('Error updating event registration:', error);
          throw error;
        }
      }

      function setupRealtimeListeners() {
        const eventsRef = window.firebase.collection(window.firebase.db, 'events');
        const eventsQuery = window.firebase.query(eventsRef, window.firebase.orderBy('created_at', 'desc'));
        
        window.firebase.onSnapshot(eventsQuery, (snapshot) => {
          const changes = snapshot.docChanges();
          changes.forEach((change) => {
            const eventData = { id: change.doc.id, ...change.doc.data() };
            if (change.type === 'added') {
              if (!appData.events.find(e => e.id === eventData.id)) appData.events.unshift(eventData);
            } else if (change.type === 'modified') {
              const index = appData.events.findIndex(e => e.id === eventData.id);
              if (index !== -1) appData.events[index] = eventData;
            } else if (change.type === 'removed') {
              appData.events = appData.events.filter(e => e.id !== eventData.id);
            }
          });
          render();
        });
        const usersRef = window.firebase.collection(window.firebase.db, 'users');
        window.firebase.onSnapshot(usersRef, (snapshot) => {
          users = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
        });
      }

      // --------------------
      // Firebase Auth
      // --------------------
      async function signUp({email,password,role,name}){
        try {
          const userCredential = await window.firebase.createUserWithEmailAndPassword(window.firebase.auth, email, password);
          const user = userCredential.user;
          const userData = { email, role, name: name || email.split('@')[0], created_at: new Date().toISOString() };
          await saveUser(userData);
          session = {email, role, name: name || email.split('@')[0], uid: user.uid};
          return session;
        } catch (error) {
          throw new Error(error.message || 'Failed to create account');
        }
      }

      async function signIn({email,password}){
        try {
          const userCredential = await window.firebase.signInWithEmailAndPassword(window.firebase.auth, email, password);
          const user = userCredential.user;
          const usersRef = window.firebase.collection(window.firebase.db, 'users');
          const q = window.firebase.query(usersRef, window.firebase.where('email', '==', user.email));
          const snapshot = await window.firebase.getDocs(q);
          if (snapshot.empty) {
            throw new Error('User data not found');
          }
          const userData = snapshot.docs[0].data();
          session = { email: userData.email, role: userData.role, name: userData.name || user.email.split('@')[0], uid: user.uid };
          return session;
        } catch (error) {
          throw new Error(error.message || 'Invalid credentials');
        }
      }

      function requireAuth(role){
        if(!session){ setToast('Please login/register first', false); window.app.navigate('#/login'); return false }
        if(role && session.role!==role){ setToast('Access denied for this role', false); return false }
        return true;
      }

      function setupAuthListener() {
        window.firebase.onAuthStateChanged(window.firebase.auth, async (user) => {
          if (user) {
            try {
              const usersRef = window.firebase.collection(window.firebase.db, 'users');
              const q = window.firebase.query(usersRef, window.firebase.where('email', '==', user.email));
              const snapshot = await window.firebase.getDocs(q);
              if (!snapshot.empty) {
                const userData = snapshot.docs[0].data();
                session = { email: userData.email, role: userData.role, name: userData.name || user.email.split('@')[0], uid: user.uid };
              }
            } catch (error) {
              console.error('Error getting user data:', error);
            }
          } else {
            session = null;
          }
          renderHeader();
        });
      }

      // All other functions (renderHeader, field, pill, etc.)
      function renderHeader(){
        const auth = session ? `
          <span class="hidden sm:flex items-center gap-2 text-slate-200">
            <i data-lucide="user" class=w-5 h-5></i>
            <span class="max-sm:hidden font-medium">${session.name || session.email}</span>
            <span class="badge bg-brand/10 text-brand">${session.role}</span>
          </span>
          <button class="btn btn-primary" onclick="window.app.navigate('#/dashboard')">Dashboard</button>
          <button class="btn btn-ghost" onclick="window.app.signOut()">Logout</button>
        ` : `
          <a href="#/login" class="btn btn-primary">Login</a>
        `;
        authArea.innerHTML = auth;
        lucide.createIcons();
      }
      
      function field(id, label, type='text', attrs=''){
        return `
          <label class="block text-sm font-medium text-slate-400">${label}
            <input id="${id}" type="${type}" class="input mt-1" ${attrs} />
          </label>`
      }
      
      function pill(text){ return <span class="badge">${text}</span> }

      // The main router engine function
      function route(path, handler){ routes[path]=handler }

      function matchRoute(){
        const hash = location.hash || '#/'
        for(const pattern in routes){
          const params = {};
          const hashParts = hash.split('?')[0].split('/');
          const patternParts = pattern.split('/');
          if(hashParts.length !== patternParts.length) continue;
          let match = true;
          for(let i=0; i<patternParts.length; i++){
            if(patternParts[i].startsWith(':')){
              params[patternParts[i].slice(1)] = hashParts[i];
            } else if(patternParts[i] !== hashParts[i]){
              match = false;
              break;
            }
          }
          if(match){
            return { handler: routes[pattern], params };
          }
        }
        return { handler: routes['#/'], params:{} };
      }

      function render(){
        const {handler, params} = matchRoute();
        handler(params);
        renderHeader();
      }

      function initializeRouter(){
        // All route definitions now live inside this function
        // Home / Search
        route('#/', () => {
          const q = new URLSearchParams(location.hash.split('?')[1]||'').get('q')||'';
          const list = appData.events.filter(e=> e.is_published && (
            !q || [e.title,e.category,e.venue].join(' ').toLowerCase().includes(q.toLowerCase())
          ));
          view.innerHTML = `
            <div class="py-10">
              <div class="text-center max-w-4xl mx-auto mb-12">
                <h1 class="text-4xl sm:text-5xl font-extrabold tracking-tight glow-text">Discover and Register for <span class="text-brand">Events</span></h1>
                <p class="text-slate-400 mt-4 text-lg">Search, register, and manage events with an engaging, interactive UI. A new era of event management is here.</p>
              </div>
              <div class="max-w-4xl mx-auto mb-10 flex gap-4">
                <input id="search" class="input flex-1" placeholder="Search by name, category, or venue" value="${q}" />
                <button class="btn btn-primary" onclick="window.app.doSearch()"><i data-lucide=search class=w-5 h-5></i>Search</button>
              </div>
              <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-8">
                ${list.map(renderEventCard).join('') || emptyState('No events found')}
              </div>
            </div>
          `;
          lucide.createIcons();
          $('#search').addEventListener('keydown', e=>{ if(e.key==='Enter') window.app.doSearch() })
        });
        
        function renderEventCard(e){
          const regCount = e.registration_count || 0;
          const slotsLeft = e.slots_available || Math.max(0, (e.max||0) - regCount);
          const isSoldOut = slotsLeft <= 0;
          return `
          <article class="card fade-in transition-transform hover:scale-[1.02]">
            <div class="flex items-start justify-between gap-4">
              <h3 class="font-bold text-xl">${e.title}</h3>
              ${e.is_published? <span class="badge bg-emerald-700 text-white">Published</span>: <span class="badge bg-amber-700 text-white">Draft</span>}
            </div>
            <p class="text-sm text-slate-400 mt-2 line-clamp-2">${e.description||''}</p>
            <div class="mt-4 text-sm flex flex-wrap gap-3 text-slate-400">
              <span class="inline-flex items-center gap-1"><i data-lucide=calendar class=w-4 h-4></i> ${new Date(e.date_time).toLocaleString()}</span>
              <span class="inline-flex items-center gap-1"><i data-lucide=map-pin class=w-4 h-4></i> ${e.venue}</span>
            </div>
            <div class="mt-4 p-3 bg-slate-800/50 rounded-lg">
              <div class="grid grid-cols-2 gap-4 text-center">
                <div>
                  <div class="text-2xl font-bold text-brand">${regCount}</div>
                  <div class="text-xs text-slate-400">Registered</div>
                </div>
                <div>
                  <div class="text-2xl font-bold ${isSoldOut ? 'text-red-400' : 'text-green-400'}">${slotsLeft}</div>
                  <div class="text-xs text-slate-400">Available</div>
                </div>
              </div>
            </div>
            <div class="mt-5 flex items-center justify-between">
              <div class="flex items-center gap-2">
                ${pill('Max: '+(e.max||0))}
                ${!isSoldOut ? <span class="badge bg-blue-700 text-white pulse-effect">Slots left: ${slotsLeft}</span> : <span class="badge bg-rose-700 text-white">Sold out</span>}
              </div>
              <a href="#/event/${e.id}" class="btn btn-primary">Details</a>
            </div>
          </article>`
        }
        
        function emptyState(text){
          return <div class="col-span-full text-center py-16 text-slate-500 text-xl"><i data-lucide="info" class="w-8 h-8 mx-auto mb-2"></i><p>${text}</p></div>
        }

        // Login & Register (separate pages)
        route('#/login', ()=>{
          view.innerHTML = `
            <div class="max-w-md mx-auto card fade-in">
              <h2 class="text-3xl font-black text-center">Login</h2>
              <p class="text-slate-400 text-center mt-2">Welcome back to <b class="glow-text">Eventx</b>.</p>
              <div class="mt-6 space-y-4">
                ${field('li_email','Email','email')}
                ${field('li_password','Password','password')}
                <button class="btn btn-primary w-full mt-4" onclick="window.app.onLogin()">Login</button>
                <p class="text-xs text-slate-400 text-center">Not registered? <a class="underline font-medium link" href="#/register">Register as a Student</a></p>
              </div>
            </div>`;
          lucide.createIcons();
        })

        route('#/register', ()=>{
          view.innerHTML = `
            <div class="max-w-md mx-auto card fade-in">
              <h2 class="text-3xl font-black text-center">Create your Student Account</h2>
              <p class="text-slate-400 text-center mt-2">Join <b class="glow-text">Eventx</b> to attend events.</p>
              <div class="mt-6 space-y-4">
                ${field('su_name','Name')}
                ${field('su_email','Email','email')}
                ${field('su_password','Password (6+ chars)','password')}
                <button class="btn btn-primary w-full mt-4" onclick="window.app.onRegister('student')">Create Account</button>
                <p class="text-xs text-slate-400 text-center">Already have an account? <a class="underline font-medium link" href="#/login">Login here</a></p>
                <p class="text-xs text-slate-400 text-center">Are you an organiser? <a class="underline font-medium link" href="#/register/organiser">Register here</a></p>
              </div>
            </div>`;
          lucide.createIcons();
        })

        route('#/register/organiser', ()=>{
          view.innerHTML = `
            <div class="max-w-md mx-auto card fade-in">
              <h2 class="text-3xl font-black text-center">Create your Organiser Account</h2>
              <p class="text-slate-400 text-center mt-2">Join <b class="glow-text">Eventx</b> to host and manage events.</p>
              <div class="mt-6 space-y-4">
                ${field('su_name','Organiser Name')}
                ${field('su_email','Email','email')}
                ${field('su_password','Password (6+ chars)','password')}
                <button class="btn btn-primary w-full mt-4" onclick="window.app.onRegister('organiser')">Create Account</button>
                <p class="text-xs text-slate-400 text-center">Already have an account? <a class="underline font-medium link" href="#/login">Login here</a></p>
                <p class="text-xs text-slate-400 text-center">Are you a student? <a class="underline font-medium link" href="#/register">Register here</a></p>
              </div>
            </div>`;
          lucide.createIcons();
        })

        // Event Details & Registration (Student)
        route('#/event/:id', (params)=>{
          const e = appData.events.find(x=>x.id===params.id);
          if(!e){ view.innerHTML = emptyState('Event not found'); return }
          const regCount = e.participants.filter(p=>p.paid).reduce((a,b)=>a+(b.qty||1),0);
          const slotsLeft = Math.max(0, (e.max||0) - regCount);

          view.innerHTML = `
            <div class="grid lg:grid-cols-3 gap-8 fade-in">
              ${e.announcement ? `
                <div class="lg:col-span-3 card bg-slate-800/80 border-slate-700">
                  <h4 class="font-bold text-slate-200 flex items-center gap-2"><i data-lucide="megaphone" class="w-5 h-5"></i>Announcement</h4>
                  <p class="text-slate-400 mt-1">${e.announcement}</p>
                </div>
              ` : ''}
              <div class="lg:col-span-2 card">
                <h2 class="text-3xl font-extrabold">${e.title}</h2>
                <p class="text-slate-400 mt-3">${e.description||''}</p>
                <div class="mt-6 flex flex-wrap gap-3 text-sm">
                  ${pill('Category: '+(e.category||'General'))}
                  ${pill('Date: '+new Date(e.date_time).toLocaleString())}
                  ${pill('Venue: '+e.venue)}
                </div>
              </div>
              <aside class="card">
                <h4 class="font-bold text-xl">Registration Details</h4>
                <div class="mt-4 flex items-center justify-between">
                  <div>
                    <div class="text-sm text-slate-400">Price</div>
                    <div class="text-2xl font-bold">₹${(e.price||0).toFixed(2)}</div>
                  </div>
                  <div>
                    <div class="text-sm text-slate-400">Slots left</div>
                    <div class="text-2xl font-bold">${slotsLeft}</div>
                  </div>
                  <div>
                    <div class="text-sm text-slate-400">Registered</div>
                    <div class="text-2xl font-bold">${regCount}</div>
                  </div>
                </div>
                <button class="btn btn-primary w-full mt-6" onclick="window.app.goRegister('${e.id}')">Register Now</button>
              </aside>
            </div>`;
          lucide.createIcons();
        })

        // Registration Wizard (student)
        route('#/register/:id', (params)=>{
          if(!requireAuth('student')) return;
          const e = appData.events.find(x=>x.id===params.id);
          if(!e || !e.is_published){ view.innerHTML = emptyState('Event not available'); return }
          view.innerHTML = `
            <div class="max-w-3xl mx-auto card fade-in">
              <h2 class="text-2xl font-bold text-center">Register — ${e.title}</h2>
              <div class="mt-6 grid sm:grid-cols-2 gap-5">
                ${field('st_name','Your Name','text','value="'+(session.name||'')+'"')}
                ${field('st_ticket','Ticket Type','text','placeholder="General"')}
                ${field('st_qty','Quantity','number',min=1 value=1 oninput="window.app.updateTotal(${(e.price||0)})")}
                <div>
                  <label class="block text-sm font-medium text-slate-400">Total Amount (₹)</label>
                  <input id="st_amount" class="input mt-1 bg-slate-800/50" readonly value="${(e.price||0).toFixed(2)}">
                </div>
              </div>
              <div class="mt-6 flex items-center justify-end gap-3">
                <button class="btn btn-primary" onclick="window.app.goPayment('${e.id}')">Proceed to Payment</button>
                <button class="btn btn-ghost" onclick="window.app.navigate('#/event/${e.id}')">Cancel</button>
              </div>
            </div>`;
        })

        // Dashboard (Organiser-only)
        route('#/dashboard', ()=>{
          if(!requireAuth()) return;
          const role = session.role;
          if(role==='organiser'){
            const myEvents = appData.events.filter(e=> e.organiser_email===session.email);
            view.innerHTML = `
              <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 mb-8">
                <div>
                  <h2 class="text-3xl font-extrabold">Organiser Dashboard</h2>
                  <p class="text-slate-400 mt-1">Create and manage your events.</p>
                </div>
                <button class="btn btn-primary" onclick="window.app.openEventModal()"><i data-lucide=plus-circle class=w-5 h-5></i>Create Event</button>
              </div>
              <div class="card overflow-x-auto">
                <table class="w-full text-sm">
                  <thead>
                    <tr class="text-left text-slate-400 border-b border-slate-700">
                      <th class="py-3 px-2">Event</th>
                      <th class="py-3 px-2">Date/Time</th>
                      <th class="py-3 px-2">Registered</th>
                      <th class="py-3 px-2">Slots Left</th>
                      <th class="py-3 px-2">Status</th>
                      <th class="py-3 px-2 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    ${myEvents.map(renderEventRow).join('') || <tr><td colspan=6 class="py-6 text-center text-slate-500">No events yet</td></tr>}
                  </tbody>
                </table>
              </div>
              <dialog id="eventModal" class="p-0 rounded-2xl w-full max-w-2xl"></dialog>
            `;
            lucide.createIcons();
          } else if(role==='student'){
            const regs = appData.events.flatMap(e=> e.participants.filter(p=>p.user_email===session.email).map(p=> ({e,p})) );
            view.innerHTML = `
              <div>
                <h2 class="text-3xl font-extrabold">My Registrations</h2>
                <div class="mt-6 card">
                  <table class="w-full text-sm">
                    <thead>
                      <tr class="text-left text-slate-400 border-b border-slate-700">
                        <th class="py-3 px-2">Event</th><th class="py-3 px-2">Date</th><th class="py-3 px-2">Venue</th><th class="py-3 px-2">Qty</th><th class="py-3 px-2">Amount</th><th class="py-3 px-2">Txn</th>
                      </tr>
                    </thead>
                    <tbody>
                      ${regs.map(r=>`<tr class="border-t border-slate-700">
                        <td class="py-4 px-2 font-semibold">${r.e.title}</td>
                        <td class="px-2">${new Date(r.e.date_time).toLocaleString()}</td>
                        <td class="px-2">${r.e.venue}</td>
                        <td class="px-2">${r.p.qty}</td>
                        <td class="px-2">₹${r.p.amount.toFixed(2)}</td>
                        <td class="px-2">${r.p.id.substring(0, 8)}...</td>
                      </tr>`).join('') || <tr><td colspan=6 class="py-6 text-center text-slate-500">No registrations yet</td></tr>}
                    </tbody>
                  </table>
                </div>
              </div>`;
          } else {
            view.innerHTML = emptyState('Unknown role');
          }
        })
        
        function renderEventRow(e){
          const regCount = e.registration_count || 0;
          const slotsLeft = e.slots_available || Math.max(0, (e.max||0) - regCount);
          return `<tr class="border-t border-slate-700">
            <td class="py-4 px-2"><div class="font-semibold">${e.title}</div><div class="text-slate-400">${e.category||'General'}</div></td>
            <td class="px-2">${new Date(e.date_time).toLocaleString()}</td>
            <td class="px-2">${regCount} / ${e.max}</td>
            <td class="px-2">${slotsLeft}</td>
            <td class="px-2">${e.is_published? <span class="badge bg-emerald-700 text-white">Published</span> : <span class="badge bg-amber-700 text-white">Draft</span>}</td>
            <td class="px-2 text-right">
              <div class="inline-flex gap-2 flex-wrap justify-end">
                <button class="btn btn-ghost p-2" onclick="window.app.openParticipants('${e.id}')"><i data-lucide=users class=w-4 h-4></i></button>
                <button class="btn btn-ghost p-2" onclick="window.app.openAnnounceModal('${e.id}')"><i data-lucide=megaphone class=w-4 h-4></i></button>
                <button class="btn btn-ghost p-2" onclick="window.app.openEventModal('${e.id}')"><i data-lucide=pencil class=w-4 h-4></i></button>
                <button class="btn btn-ghost p-2" onclick="window.app.togglePublish('${e.id}')"><i data-lucide=${e.is_published?'eye-off':'eye'} class=w-4 h-4></i></button>
                <button class="btn btn-ghost p-2 text-rose-500 hover:bg-white/10" onclick="window.app.deleteEvent('${e.id}')"><i data-lucide=trash2 class=w-4 h-4></i></button>
              </div>
            </td>
          </tr>`
        }

        function slug(s){ return s.toLowerCase().replace(/[^a-z0-9]+/g,'-').replace(/(^-|-$)/g,'') }

        // Minimal Test Suite (run via #/tests)
        route('#/tests', ()=>{
          const results = [];
          function assert(name, fn){ try { fn(); results.push({name, ok:true}); } catch(err){ results.push({name, ok:false, err: String(err)}) } }
          const usersBackup = JSON.parse(JSON.stringify(users));
          const sessionBackup = JSON.parse(JSON.stringify(session));
          assert('slug("Hello, World!") → "hello-world"', ()=>{ if(slug('Hello, World!') !== 'hello-world') throw new Error('slug failed'); });
          assert('uid() generates 100 unique IDs', ()=>{ const set = new Set(Array.from({length:100}, ()=> uid())); if(set.size !== 100) throw new Error('uid not unique'); });
          assert('signIn with bad credentials throws', ()=>{ let threw = false; try { signIn({email:'no@no.com', password:'x'}) } catch { threw = true } if(!threw) throw new Error('expected throw'); });
          assert('requireAuth when logged out returns false and redirects', ()=>{ const oldHash = location.hash; session = null; const ok = requireAuth('student'); if(ok) throw new Error('should be false'); if(location.hash !== '#/login') throw new Error('did not navigate to login'); window.app.navigate(oldHash); });
          Object.assign(users, usersBackup);
          session = sessionBackup;
          const passed = results.filter(r=>r.ok).length;
          const failed = results.length - passed;
          view.innerHTML = `
            <div class="card">
              <h2 class="text-2xl font-bold">Test Results</h2>
              <p class="text-sm text-slate-400">${passed} passed, ${failed} failed</p>
              <ul class="mt-4 space-y-2 text-sm">
                ${results.map(r=> <li>${r.ok ? '✅' : '❌'} ${r.name} ${r.ok ? '' : '— '+r.err}</li>).join('')}
              </ul>
              <div class="mt-6"><a class="btn btn-primary" href="#/">Back to app</a></div>
            </div>`;
        });
      }
    
      // Final Initialization
      $('#year').textContent = new Date().getFullYear();
      setupAuthListener();
      await loadEvents();
      await loadUsers();
      initializeRouter(); // Call the router initialization function here
      setupRealtimeListeners();
      render();
    }

    // Use a simple, reliable loading method
    try {
      const { initializeApp } = await import('https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js');
      const { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, onAuthStateChanged } = await import('https://www.gstatic.com/firebasejs/10.7.1/firebase-auth.js');
      const { getFirestore, collection, addDoc, getDocs, getDoc, doc, updateDoc, deleteDoc, query, where, orderBy, onSnapshot } = await import('https://www.gstatic.com/firebasejs/10.7.1/firebase-firestore.js');
      
      // Initialize Firebase and make it global
      const app = initializeApp(firebaseConfig);
      const auth = getAuth(app);
      const db = getFirestore(app);
      window.firebase = { auth, db, initializeApp, getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, signOut, onAuthStateChanged, getFirestore, collection, addDoc, getDocs, getDoc, doc, updateDoc, deleteDoc, query, where, orderBy, onSnapshot };
      
      // Run the main application logic
      main();

    } catch (error) {
      console.error('🚨 Critical failure: Firebase could not be loaded.', error);
      document.body.innerHTML = `
        <div class="min-h-screen bg-slate-900 flex items-center justify-center">
          <div class="text-center max-w-md mx-auto p-8">
            <div class="text-red-500 text-6xl mb-4">⚠</div>
            <h1 class="text-2xl font-bold text-white mb-4">Firebase Connection Failed</h1>
            <p class="text-slate-400 mb-6">Unable to load Firebase from any CDN source. Please check your internet connection and refresh the page.</p>
            <button onclick="location.reload()" class="bg-purple-600 hover:bg-purple-700 text-white px-6 py-2 rounded-lg">
              Refresh Page
            </button>
          </div>
        </div>
      `;
    }
  </script>
</body>
</html>
